package top.yunmouren.browserblock.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import top.yunmouren.browserblock.ModBlocks;

public class BrowserNodeBlockEntity extends BlockEntity {
    @Nullable
    private BlockPos masterPos = null;
    private int relX = 0, relY = 0;

    public BrowserNodeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BROWSER_NODE_ENTITY.get(), pos, state);
    }

    public void setMasterInfo(BlockPos masterPos, int rx, int ry) {
        this.masterPos = masterPos;
        this.relX = rx;
        this.relY = ry;
        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public void clearMaster() {
        this.masterPos = null;
        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("MX")) {
            masterPos = new BlockPos(tag.getInt("MX"), tag.getInt("MY"), tag.getInt("MZ"));
        } else {
            masterPos = null;
        }
        relX = tag.getInt("RX");
        relY = tag.getInt("RY");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (masterPos != null) {
            tag.putInt("MX", masterPos.getX());
            tag.putInt("MY", masterPos.getY());
            tag.putInt("MZ", masterPos.getZ());
        }
        tag.putInt("RX", relX);
        tag.putInt("RY", relY);
    }

    @Nullable
    public BrowserMasterBlockEntity getMaster() {
        if (level == null || masterPos == null) return null;
        BlockEntity be = level.getBlockEntity(masterPos);
        return be instanceof BrowserMasterBlockEntity ? (BrowserMasterBlockEntity) be : null;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    public int getRelX() { return relX; }
    public int getRelY() { return relY; }
}