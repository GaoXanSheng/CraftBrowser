package top.yunmouren.browserblock.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import top.yunmouren.craftbrowser.client.config.Config;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class StructureHelper {

    public record Result(boolean ok, String message) {
        static Result ok(String message) {
            return new Result(true, message);
        }

        static Result fail(String message) {
            return new Result(false, message);
        }
    }

    public static Result reformStructure(Level level, BlockPos startPos) {
        Set<BlockPos> nodes = new HashSet<>();
        Set<BlockPos> masters = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.push(startPos);
        int maxBlocks = Config.CLIENT.maxStructureBlocks.get();

        while (!queue.isEmpty()) {
            BlockPos current = queue.pop();
            if (!visited.add(current)) continue;
            if (visited.size() > maxBlocks) {
                return Result.fail("structure too large, max " + maxBlocks + " blocks");
            }

            boolean isMaster = level.getBlockState(current).getBlock() instanceof BrowserMasterBlock;
            boolean isNode = level.getBlockState(current).getBlock() instanceof BrowserNodeBlock;
            if (!isMaster && !isNode) continue;

            if (isMaster) {
                masters.add(current);
            } else {
                nodes.add(current);
            }

            queue.push(current.above());
            queue.push(current.below());
            queue.push(current.north());
            queue.push(current.south());
            queue.push(current.east());
            queue.push(current.west());
        }

        if (masters.isEmpty()) {
            return Result.fail("no master block connected");
        }

        BlockPos masterPos = masters.iterator().next();
        Direction facing = level.getBlockState(masterPos).getValue(BrowserMasterBlock.FACING);

        boolean depthIsZ = facing.getAxis() == Direction.Axis.Z;
        int plane = depthIsZ ? masterPos.getZ() : masterPos.getX();
        masters.removeIf(p -> (depthIsZ ? p.getZ() : p.getX()) != plane);
        nodes.removeIf(p -> (depthIsZ ? p.getZ() : p.getX()) != plane);

        if (masters.size() > 1) {
            return Result.fail(masters.size() + " master blocks in one structure, keep exactly one");
        }

        if (!(level.getBlockEntity(masterPos) instanceof BrowserMasterBlockEntity master)) {
            return Result.fail("master block has no block entity");
        }

        Set<BlockPos> allBlocks = new HashSet<>(nodes);
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
        int masterRelX;
        int masterRelY = masterPos.getY() - minY;

        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            width = maxX - minX + 1;
            masterRelX = facing == Direction.NORTH ? maxX - masterPos.getX() : masterPos.getX() - minX;
        } else {
            width = maxZ - minZ + 1;
            masterRelX = facing == Direction.WEST ? masterPos.getZ() - minZ : maxZ - masterPos.getZ();
        }

        int expected = width * height;
        if (allBlocks.size() != expected) {
            return Result.fail("structure is not a full rectangle: "
                    + allBlocks.size() + " blocks but " + width + "x" + height + " needs " + expected);
        }

        Set<BlockPos> oldNodes = master.getNodePositions();
        for (BlockPos oldNode : oldNodes) {
            if (!nodes.contains(oldNode) && level.getBlockEntity(oldNode) instanceof BrowserNodeBlockEntity oldNodeBe) {
                oldNodeBe.clearMaster();
            }
        }

        master.setStructureInfo(width, height, masterRelX, masterRelY, nodes);
        master.setChanged();
        level.sendBlockUpdated(masterPos, level.getBlockState(masterPos), level.getBlockState(masterPos), 3);

        for (BlockPos p : nodes) {
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

        return Result.ok(width + "x" + height + " with " + nodes.size() + " nodes at " + masterPos.toShortString());
    }
}
