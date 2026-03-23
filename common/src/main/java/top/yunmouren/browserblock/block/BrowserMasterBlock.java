package top.yunmouren.browserblock.block;

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
import top.yunmouren.browserblock.ModBlocks;
import top.yunmouren.browserblock.client.BrowserClientHooks;

public class BrowserMasterBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

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
            StructureHelper.reformStructure(level, pos, state.getValue(FACING));
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

        if (hit.getDirection() != state.getValue(FACING)) {
            return InteractionResult.PASS;
        }

        if (!(level.getBlockEntity(pos) instanceof BrowserMasterBlockEntity be)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            if (level.isClientSide) {
                BrowserClientHooks.openBrowserScreen(be);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            boolean success = StructureHelper.reformStructure(level, pos, state.getValue(FACING));
            if (success) {
                player.displayClientMessage(
                        Component.literal("§aBrowser structure activated!"), true
                );
            } else {
                player.displayClientMessage(
                        Component.literal("§cFailed to form a valid browser structure. Make sure there is only one Master Block."),
                        true
                );
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // 打掉主方块时，通知所有相连的子节点清空 Master
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof BrowserMasterBlockEntity master) {
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