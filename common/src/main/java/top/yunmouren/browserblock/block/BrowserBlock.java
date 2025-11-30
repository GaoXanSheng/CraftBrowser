package top.yunmouren.browserblock.block;

import net.minecraft.client.Minecraft;
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
import top.yunmouren.browserblock.client.BrowserUrlScreen;

public class BrowserBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BrowserBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrowserBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            StructureHelper.updateStructure(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = pos.relative(dir);
                    if (level.getBlockState(neighbor).is(this)) {
                        StructureHelper.updateStructure(level, neighbor);
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BrowserBlockEntity browserEntity)) {
            return InteractionResult.PASS;
        }

        BrowserBlockEntity master = browserEntity.getMaster();
        if (master == null) return InteractionResult.FAIL;

        if (hit.getDirection() != state.getValue(FACING)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (level.isClientSide) {
                Minecraft.getInstance().setScreen(new BrowserUrlScreen(master)); // 打开 Master 的配置
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.isClientSide) {
            Direction facing = state.getValue(FACING);
            handleClick(browserEntity, hit, facing);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void handleClick(BrowserBlockEntity be, BlockHitResult hit, Direction facing) {
        BrowserBlockEntity master = be.getMaster();
        if (master == null) return;

        double dx = hit.getLocation().x - be.getBlockPos().getX();
        double dy = hit.getLocation().y - be.getBlockPos().getY();
        double dz = hit.getLocation().z - be.getBlockPos().getZ();

        double localHitX;

        switch (facing) {
            case SOUTH: localHitX = dx; break;
            case NORTH: localHitX = 1.0 - dx; break;
            case WEST:  localHitX = dz; break;
            case EAST:  localHitX = 1.0 - dz; break;
            default: return;
        }

        double totalWidth = master.getWidth();
        double totalHeight = master.getHeight();

        double globalX = be.getRelX() + localHitX;
        double globalYFromBottom = be.getRelY() + dy;
        double globalYFromTop = totalHeight - globalYFromBottom;

        int pixelW = (int) (totalWidth * 64);
        int pixelH = (int) (totalHeight * 64);

        int browserX = (int) ((globalX / totalWidth) * pixelW);
        int browserY = (int) ((globalYFromTop / totalHeight) * pixelH);

        master.sendClickInput(browserX, browserY);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}