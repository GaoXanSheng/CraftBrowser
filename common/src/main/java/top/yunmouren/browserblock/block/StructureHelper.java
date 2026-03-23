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

    public static void triggerNearbyScan(Level level, BlockPos pos, Direction facing) {
        Stack<BlockPos> toVisit = new Stack<>();
        toVisit.push(pos);
        Set<BlockPos> visited = new HashSet<>();

        while (!toVisit.isEmpty()) {
            BlockPos current = toVisit.pop();
            if (visited.contains(current)) continue;
            visited.add(current);

            if (visited.size() > 400) break;

            BlockState state = level.getBlockState(current);
            Block block = state.getBlock();

            if (!(block instanceof BrowserMasterBlock || block instanceof BrowserNodeBlock)) continue;
            if (state.getValue(BrowserMasterBlock.FACING) != facing) continue;

            if (block instanceof BrowserMasterBlock) {
                reformStructure(level, current, facing);
                return;
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
    }
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
                if (masterPos != null && !masterPos.equals(current)) return false;
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
        int masterRelX = 0;
        int masterRelY = masterPos.getY() - minY;

        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            width = maxX - minX + 1;
            if (facing == Direction.NORTH) masterRelX = maxX - masterPos.getX();
            else masterRelX = masterPos.getX() - minX;
        } else {
            width = maxZ - minZ + 1;
            if (facing == Direction.WEST) masterRelX = masterPos.getZ() - minZ;
            else masterRelX = maxZ - masterPos.getZ();
        }
        if (level.getBlockEntity(masterPos) instanceof BrowserMasterBlockEntity master) {
            Set<BlockPos> oldNodes = master.getNodePositions();
            for (BlockPos oldNode : oldNodes) {
                if (!connectedNodes.contains(oldNode)) {
                    if (level.getBlockEntity(oldNode) instanceof BrowserNodeBlockEntity oldNodeBe) {
                        oldNodeBe.clearMaster();
                    }
                }
            }

            master.setStructureInfo(width, height, masterRelX, masterRelY, connectedNodes);
            master.setChanged();
            level.sendBlockUpdated(masterPos, level.getBlockState(masterPos), level.getBlockState(masterPos), 3);
        }
        for (BlockPos p : connectedNodes) {
            if (level.getBlockEntity(p) instanceof BrowserNodeBlockEntity be) {
                int relY = p.getY() - minY;
                int relX = 0;
                switch (facing) {
                    case SOUTH -> relX = p.getX() - minX;
                    case NORTH -> relX = maxX - p.getX();
                    case WEST -> relX = p.getZ() - minZ;
                    case EAST -> relX = maxZ - p.getZ();
                }
                be.setMasterInfo(masterPos, relX, relY);
            }
        }
        return true;
    }
}