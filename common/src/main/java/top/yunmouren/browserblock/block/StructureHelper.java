package top.yunmouren.browserblock.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class StructureHelper {

    public static void reformStructure(Level level, BlockPos startPos, Direction facing) {
        // 1. 广度优先搜索 (Flood Fill)
        Set<BlockPos> connected = new HashSet<>();
        Stack<BlockPos> toVisit = new Stack<>();
        toVisit.push(startPos);

        while (!toVisit.isEmpty()) {
            BlockPos current = toVisit.pop();
            if (connected.contains(current)) continue;

            BlockState state = level.getBlockState(current);
            if (!(state.getBlock() instanceof BrowserBlock) || state.getValue(BrowserBlock.FACING) != facing) {
                continue;
            }

            connected.add(current);
            toVisit.push(current.above());
            toVisit.push(current.below());
            if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                toVisit.push(current.east());
                toVisit.push(current.west());
            } else {
                toVisit.push(current.north());
                toVisit.push(current.south());
            }
        }

        if (connected.isEmpty()) return;

        // 2. 计算边界
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (BlockPos p : connected) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxZ = Math.max(maxZ, p.getZ());
        }

        // 3. 选举 Master (坐标最小点)
        BlockPos newMasterPos = new BlockPos(minX, minY, minZ);

        int height = maxY - minY + 1;
        int width;
        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            width = maxX - minX + 1;
        } else {
            width = maxZ - minZ + 1;
        }

        // 4. 更新所有方块
        for (BlockPos p : connected) {
            if (level.getBlockEntity(p) instanceof BrowserBlockEntity be) {
                int relY = p.getY() - minY;
                int relX = switch (facing) {
                    case SOUTH -> p.getX() - minX;
                    case NORTH -> maxX - p.getX();
                    case WEST -> p.getZ() - minZ;
                    case EAST -> maxZ - p.getZ();
                    default -> 0;
                };

                // 更新数据 -> 触发 NBT Sync -> 客户端 load -> 客户端 Resize
                be.setStructureInfo(newMasterPos, width, height, relX, relY);

                // 如果该方块之前是独立的 Master，现在变成 Slave，销毁其浏览器
                if (!p.equals(newMasterPos)) {
                    be.destroyBrowser();
                } else {
                    // 如果它是 Master，确保它的状态是“应该拥有浏览器”
                    // 如果之前就是 Master 且浏览器开着，NBT load 会处理 Resize
                    // 如果之前不是 Master，NBT load 会发现 ID/状态变化，触发 clientTick 初始化
                    if (be.getBrowserTextureId() == -1) {
                        be.markForInitialization();
                    }
                }
            }
        }
    }
}