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
        // 让屏幕正对着玩家
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
            // 修复：破坏方块后，通知上下左右的邻居更新结构
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

        // Shift+右键：打开设置
        if (player.isShiftKeyDown()) {
            if (level.isClientSide) {
                Minecraft.getInstance().setScreen(new BrowserUrlScreen(browserEntity));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // 正常右键：点击屏幕
        if (level.isClientSide) {
            Direction facing = state.getValue(FACING);
            handleClick(browserEntity, hit, facing);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void handleClick(BrowserBlockEntity be, BlockHitResult hit, Direction facing) {
        BrowserBlockEntity master = be.getMaster();
        if (master == null) return;

        // 1. 获取方块内的点击坐标 (0.0 ~ 1.0)
        double dx = hit.getLocation().x - be.getBlockPos().getX();
        double dy = hit.getLocation().y - be.getBlockPos().getY();
        double dz = hit.getLocation().z - be.getBlockPos().getZ();

        double localHitX;

        // 2. 根据朝向计算 localHitX (从屏幕内容的左边到右边)
        // 必须与 StructureHelper 的逻辑严格对应
        switch (facing) {
            case SOUTH: localHitX = dx; break;         // West -> East
            case NORTH: localHitX = 1.0 - dx; break;   // East -> West
            case WEST:  localHitX = dz; break;         // North -> South
            case EAST:  localHitX = 1.0 - dz; break;   // South -> North
            default: return;
        }

        // 3. 计算全局坐标
        double totalWidth = master.getWidth();
        double totalHeight = master.getHeight();

        // X轴：Block相对位置 + 块内偏移
        double globalX = be.getRelX() + localHitX;

        // Y轴：Minecraft是从下往上(dy=0是底部)，浏览器是从上往下
        // GlobalY(从底往上) = relY + dy
        double globalYFromBottom = be.getRelY() + dy;
        double globalYFromTop = totalHeight - globalYFromBottom;

        // 4. 转换为像素坐标 (假设 1格=64像素)
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