package top.yunmouren.browserblock.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import top.yunmouren.craftbrowser.client.browser.api.BrowserSubprocess;
import top.yunmouren.craftbrowser.client.config.Config;

import static top.yunmouren.browserblock.registry.ModBlocks.BROWSER_BLOCK_ENTITY;

public class BrowserBlockEntity extends BlockEntity {

    private BlockPos masterPos;
    private int relX = 0;
    private int relY = 0;
    private int width = 1;
    private int height = 1;
    private String currentUrl = Config.CLIENT.customURL.get();

    // Client-side only: Manages the dedicated browser process for this block.
    @Nullable
    private transient BrowserSubprocess browserSubprocess;

    public BrowserBlockEntity(BlockPos pos, BlockState state) {
        super(BROWSER_BLOCK_ENTITY.get(), pos, state);
    }

    // --- Browser Process Management (Client-Side) ---

    /**
     * Lazily initializes and returns the browser subprocess for this master block.
     * This ensures the process is only created when needed on the client side.
     */
    @Nullable
    private BrowserSubprocess getBrowserSubprocess() {
        if (this.level == null || !this.level.isClientSide || !isMaster()) {
            return null;
        }
        if (this.browserSubprocess == null) {
            int pixelWidth = this.width * 64;
            int pixelHeight = this.height * 64;
            // Generate a unique ID for the spout stream based on the block's position
            String spoutId = "browserblock_" + this.worldPosition.toShortString().replaceAll("[^\\w-]", "_");
            this.browserSubprocess = new BrowserSubprocess(this.currentUrl, pixelWidth, pixelHeight, spoutId, 60);
        }
        return this.browserSubprocess;
    }
    public void onLoad() {
        if (level != null && level.isClientSide && isMaster()) {
            getBrowserSubprocess();
        }
    }
    /**
     * Called by the renderer to get the current texture ID.
     * This method is now very lightweight and just retrieves the latest texture from the subprocess.
     */
    public int getBrowserTextureId() {
        int pixelWidth = this.width * 64;
        int pixelHeight = this.height * 64;
        if (!isMaster()) {

            BrowserBlockEntity master = getMaster();
            return master != null ? master.getBrowserTextureId() : -1;
        }

        BrowserSubprocess subprocess = getBrowserSubprocess();
        if (subprocess != null && subprocess.getRender() != null) {
            // Assumes getRender() returns an object that can provide the texture ID
            // without triggering a new, blocking render operation.
            return subprocess.getRender().render(pixelWidth, pixelHeight);
        }

        return -1;
    }

    // --- Block Entity Lifecycle ---

    @Override
    public void setRemoved() {
        super.setRemoved();
        // Clean up the browser process when the block is removed on the client.
        if (level != null && level.isClientSide && browserSubprocess != null) {
            browserSubprocess.lifecycleManager.onClose();
            browserSubprocess = null;
        }
    }

    // --- Structure and URL Management ---

    public void setStructureData(BlockPos masterPos, int x, int y, int w, int h) {
        this.masterPos = masterPos;
        this.relX = x;
        this.relY = y;
        this.width = w;
        this.height = h;
        setChanged();

        // If the size changes, update the browser subprocess viewport.
        if (isMaster() && level != null && level.isClientSide) {
            BrowserSubprocess subprocess = getBrowserSubprocess();
            if (subprocess != null) {
                subprocess.lifecycleManager.resizeViewport(w * 64, h * 64);
            }
        }
    }

    public void setUrl(String newUrl) {
        if (!isMaster()) {
            BrowserBlockEntity master = getMaster();
            if (master != null) master.setUrl(newUrl);
            return;
        }

        this.currentUrl = newUrl;
        this.setChanged();

        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }

        // On the client, tell the dedicated browser process to navigate to the new URL.
        if (level != null && level.isClientSide) {
            BrowserSubprocess subprocess = getBrowserSubprocess();
            if (subprocess != null) {
                subprocess.pageHandler.loadUrl(newUrl);
            }
        }
    }

    public String getUrl() {
        if (!isMaster()) {
            BrowserBlockEntity master = getMaster();
            return master != null ? master.getUrl() : "";
        }
        return this.currentUrl;
    }

    // --- Input Handling ---

    public void sendClickInput(int x, int y) {
        if (!isMaster()) {
            BrowserBlockEntity master = getMaster();
            if (master != null) master.sendClickInput(x, y);
            return;
        }
        BrowserSubprocess subprocess = getBrowserSubprocess();
        if (subprocess != null) {
            subprocess.mouseHandler.mousePress(x,y,0);
            subprocess.mouseHandler.mouseRelease(x,y,0);
        }
    }

    // --- Boilerplate Getters and Data Handling ---

    public boolean isMaster() {
        return this.worldPosition.equals(masterPos);
    }

    @Nullable
    public BrowserBlockEntity getMaster() {
        if (level == null || masterPos == null) return null;
        if (isMaster()) return this;
        BlockEntity be = level.getBlockEntity(masterPos);
        return be instanceof BrowserBlockEntity ? (BrowserBlockEntity) be : null;
    }

    public AABB getRenderBoundingBox() {
        return new net.minecraft.world.phys.AABB(
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY
        );
    }

    public int getRelX() {
        return relX;
    }

    public int getRelY() {
        return relY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (masterPos != null) {
            tag.putInt("MasterX", masterPos.getX());
            tag.putInt("MasterY", masterPos.getY());
            tag.putInt("MasterZ", masterPos.getZ());
        }
        tag.putInt("RelX", relX);
        tag.putInt("RelY", relY);
        tag.putInt("Width", width);
        tag.putInt("Height", height);
        tag.putString("BrowserUrl", currentUrl);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("MasterX")) {
            masterPos = new BlockPos(tag.getInt("MasterX"), tag.getInt("MasterY"), tag.getInt("MasterZ"));
        }
        relX = tag.getInt("RelX");
        relY = tag.getInt("RelY");
        width = tag.getInt("Width");
        height = tag.getInt("Height");
        if (tag.contains("BrowserUrl")) {
            this.currentUrl = tag.getString("BrowserUrl");
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = saveWithoutMetadata();
        tag.putString("BrowserUrl", currentUrl);
        return tag;
    }
}
