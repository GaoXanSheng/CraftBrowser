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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import top.yunmouren.browserblock.ModBlocks;
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
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide) return null;
        if (type == ModBlocks.BROWSER_BLOCK_ENTITY.get()) {
            return (level1, pos, state1, blockEntity) -> {
                if (blockEntity instanceof BrowserBlockEntity browserEntity) {
                    BrowserBlockEntity.clientTick(level1, pos, state1, browserEntity);
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
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BrowserBlockEntity browserEntity) {
                if (browserEntity.isMaster()) {
                    browserEntity.destroyBrowser();
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (hit.getDirection() != state.getValue(FACING)) {
            return InteractionResult.PASS;
        }

        if (level.getBlockEntity(pos) instanceof BrowserBlockEntity be) {
            BrowserBlockEntity master = be.getMaster();
            boolean isOrphan = (master == null);

            if (player.isShiftKeyDown()) {
                if (isOrphan) {
                    if (!level.isClientSide) {
                        StructureHelper.reformStructure(level, pos, state.getValue(FACING));
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§a屏幕结构已更新 (Screen Resized)"), true);
                    }
                } else {
                    if (level.isClientSide) {
                        Minecraft.getInstance().setScreen(new BrowserUrlScreen(master));
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            if (isOrphan) {
                if (!level.isClientSide) {
                    StructureHelper.reformStructure(level, pos, state.getValue(FACING));
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§a屏幕结构已初始化"), true);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            if (level.isClientSide) {
                handleClick(be, hit, state.getValue(FACING));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
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