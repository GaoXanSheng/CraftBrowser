package top.yunmouren.browserblock.block;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import top.yunmouren.browserblock.ModBlocks;
import top.yunmouren.browserblock.network.BrowserBlockNetworkHandler;

public class BrowserMasterBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final Logger LOGGER = LogUtils.getLogger();

    public BrowserMasterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrowserMasterBlockEntity(pos, state);
    }
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) {
            if (placer instanceof Player player && level.getBlockEntity(pos) instanceof BrowserMasterBlockEntity be) {
                if (be.getOwner() == null) be.setOwner(player.getUUID());
            }
            StructureHelper.Result result = StructureHelper.reformStructure(level, pos);
            LOGGER.info("[CraftBrowser] Structure scan at {}: {}", pos.toShortString(), result.message());
        }
    }
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide) return null;
        if (type == ModBlocks.BROWSER_MASTER_ENTITY.get()) {
            return (level1, pos, state1, blockEntity) -> {
                if (blockEntity instanceof BrowserMasterBlockEntity browserEntity) {
                    BrowserMasterBlockEntity.clientTick(level1, pos, state1, browserEntity);
                }
            };
        }
        return null;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {

        if (!(level.getBlockEntity(pos) instanceof BrowserMasterBlockEntity be)) {
            return InteractionResult.PASS;
        }

        if (hit.getDirection() != state.getValue(FACING)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (level.isClientSide) {
                BrowserBlockNetworkHandler.requestState(pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        be.handleClick(pos, be.getMasterRelX(), be.getMasterRelY(), hit);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof BrowserMasterBlockEntity master) {
                master.destroyBrowser();
                for (BlockPos nodePos : master.getNodePositions()) {
                    if (level.getBlockEntity(nodePos) instanceof BrowserNodeBlockEntity node) {
                        node.clearMaster();
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}