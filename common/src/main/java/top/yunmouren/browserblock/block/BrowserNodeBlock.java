package top.yunmouren.browserblock.block;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class BrowserNodeBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final Logger LOGGER = LogUtils.getLogger();

    public BrowserNodeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrowserNodeBlockEntity(pos, state);
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

        if (level.getBlockEntity(pos) instanceof BrowserNodeBlockEntity be) {
            BrowserMasterBlockEntity master = be.getMaster();
            if (master != null) {
                master.handleClick(be.getBlockPos(), be.getRelX(), be.getRelY(), hit);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide) return;
        for (Direction d : Direction.values()) {
            Block neighbor = level.getBlockState(pos.relative(d)).getBlock();
            if (neighbor instanceof BrowserMasterBlock || neighbor instanceof BrowserNodeBlock) {
                StructureHelper.Result result = StructureHelper.reformStructure(level, pos);
                LOGGER.info("[CraftBrowser] Structure scan at {}: {}", pos.toShortString(), result.message());
                return;
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof BrowserNodeBlockEntity node) {
                BrowserMasterBlockEntity master = node.getMaster();
                if (master != null) {
                    for (BlockPos nPos : master.getNodePositions()) {
                        if (level.getBlockEntity(nPos) instanceof BrowserNodeBlockEntity nBe) {
                            nBe.clearMaster();
                        }
                    }
                    master.setStructureInfo(1, 1, 0, 0, java.util.Set.of());
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}