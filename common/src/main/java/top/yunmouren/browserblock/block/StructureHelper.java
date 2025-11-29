package top.yunmouren.browserblock.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.HashSet;
import java.util.Set;

public class StructureHelper {

    public static void updateStructure(Level level, BlockPos startPos) {
        if (level.isClientSide) return;

        BlockState startState = level.getBlockState(startPos);
        if (!(startState.getBlock() instanceof BrowserBlock)) return;
        Direction facing = startState.getValue(BrowserBlock.FACING);

        // 1. 寻找所有连接且朝向一致的方块
        Set<BlockPos> connected = new HashSet<>();
        findConnectedBlocks(level, startPos, facing, connected);

        if (connected.isEmpty()) return;

        // 2. 计算物理边界
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (BlockPos pos : connected) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        // 3. 确定 MasterPos (作为数据锚点) 和 结构尺寸
        BlockPos masterPos = new BlockPos(minX, minY, minZ);
        int height = maxY - minY + 1;
        int width;

        // 判断结构是沿 X轴 还是 Z轴 延伸
        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            width = maxX - minX + 1;
        } else {
            width = maxZ - minZ + 1;
        }

        // 4. 更新每个方块的数据
        for (BlockPos pos : connected) {
            if (level.getBlockEntity(pos) instanceof BrowserBlockEntity be) {
                int relY = pos.getY() - minY; // Y 轴总是从下往上
                int relX = 0;

                // 核心修复：根据朝向计算 relX (0 = 屏幕内容的最左侧)
                // 注意：这里需要配合 Renderer 的旋转逻辑，防止画面镜像
                switch (facing) {
                    case SOUTH: // 面向 +Z，玩家看过去左边是 West (minX)
                        relX = pos.getX() - minX;
                        break;
                    case NORTH: // 面向 -Z，玩家看过去左边是 East (maxX)
                        relX = maxX - pos.getX();
                        break;
                    case WEST:  // 面向 -X，玩家看过去左边是 North (minZ)
                        relX = pos.getZ() - minZ;
                        break;
                    case EAST:  // 面向 +X，玩家看过去左边是 South (maxZ)
                        relX = maxZ - pos.getZ();
                        break;
                }

                be.setStructureData(masterPos, relX, relY, width, height);

                // 强制通知客户端更新 (解决紫黑块问题)
                BlockState state = level.getBlockState(pos);
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
    }

    private static void findConnectedBlocks(Level level, BlockPos pos, Direction requiredFacing, Set<BlockPos> visited) {
        if (visited.contains(pos)) return;

        BlockState state = level.getBlockState(pos);
        // 必须是浏览器方块且朝向必须完全一致
        if (!(state.getBlock() instanceof BrowserBlock) || state.getValue(BrowserBlock.FACING) != requiredFacing) {
            return;
        }

        visited.add(pos);

        // 递归搜索四周
        findConnectedBlocks(level, pos.above(), requiredFacing, visited);
        findConnectedBlocks(level, pos.below(), requiredFacing, visited);
        findConnectedBlocks(level, pos.north(), requiredFacing, visited);
        findConnectedBlocks(level, pos.south(), requiredFacing, visited);
        findConnectedBlocks(level, pos.west(), requiredFacing, visited);
        findConnectedBlocks(level, pos.east(), requiredFacing, visited);
    }
}