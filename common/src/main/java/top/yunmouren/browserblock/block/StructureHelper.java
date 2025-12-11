package top.yunmouren.browserblock.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class StructureHelper {

    public static boolean reformStructure(Level level, BlockPos startPos, Direction facing) {
        Set<BlockPos> connectedNodes = new HashSet<>();
        BlockPos masterPos = null;

        Stack<BlockPos> toVisit = new Stack<>();
        toVisit.push(startPos);
        Set<BlockPos> visited = new HashSet<>();

        while (!toVisit.isEmpty()) {
            BlockPos current = toVisit.pop();
            if (visited.contains(current)) continue;
            visited.add(current);

            BlockState state = level.getBlockState(current);
            Block block = state.getBlock();

            boolean isMaster = block instanceof BrowserMasterBlock;
            boolean isNode = block instanceof BrowserNodeBlock;

            if (!isMaster && !isNode) continue;
            if (state.getValue(BrowserNodeBlock.FACING) != facing) continue;

            if (isMaster) {
                if (masterPos != null && !masterPos.equals(current)) {
                    return false; // Found more than one master
                }
                masterPos = current;
            } else {
                connectedNodes.add(current);
            }

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

        if (masterPos == null) return false;

        Set<BlockPos> allBlocks = new HashSet<>(connectedNodes);
        allBlocks.add(masterPos);

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (BlockPos p : allBlocks) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxZ = Math.max(maxZ, p.getZ());
        }

        int height = maxY - minY + 1;
        int width;
        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            width = maxX - minX + 1;
        } else {
            width = maxZ - minZ + 1;
        }

        if (level.getBlockEntity(masterPos) instanceof BrowserMasterBlockEntity master) {
            master.setStructureInfo(width, height, connectedNodes);
        }

        for (BlockPos p : connectedNodes) {
            if (level.getBlockEntity(p) instanceof BrowserNodeBlockEntity be) {
                int relY = p.getY() - minY;
                int relX = switch (facing) {
                    case SOUTH -> p.getX() - minX;
                    case NORTH -> maxX - p.getX();
                    case WEST -> p.getZ() - minZ;
                    case EAST -> maxZ - p.getZ();
                    default -> 0;
                };
                be.setMasterInfo(masterPos, relX, relY);
            }
        }
        return true;
    }
}