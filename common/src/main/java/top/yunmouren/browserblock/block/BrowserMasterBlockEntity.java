package top.yunmouren.browserblock.block;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import top.yunmouren.browserblock.ModBlocks;
import top.yunmouren.craftbrowser.client.browser.Controller.IBrowserController;
import top.yunmouren.craftbrowser.client.browser.Core.BrowserRender;
import top.yunmouren.craftbrowser.client.browser.api.BrowserAPI;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BrowserMasterBlockEntity extends BlockEntity {
    private int width = 1;
    private int height = 1;
    private int masterRelX = 0;
    private int masterRelY = 0;

    private String currentUrl = "https://example.com/";
    private int pixelsPerBlock = 64;
    @Nullable
    private UUID owner;
    private final Object browserLock = new Object();
    @Nullable
    private IBrowserController browserSubprocess;
    private boolean isLoading = false;
    private boolean hasInitialized = false;
    private int initTimer = 0;
    private static final int INIT_DELAY = 10;

    private final Set<BlockPos> nodePositions = new HashSet<>();
    private volatile BrowserRender browserRender;
    private double volume = 1.0;
    public Set<BlockPos> getNodePositions() {
        return this.nodePositions;
    }

    public double getVolume() {
        return this.volume;
    }

    @Nullable
    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        this.setChanged();
    }

    @Nullable
    public IBrowserController getBrowserController() {
        synchronized (browserLock) {
            return this.browserSubprocess;
        }
    }

    public BrowserMasterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BROWSER_MASTER_ENTITY.get(), pos, state);
    }

    public int getPixelW() {
        return this.width * this.pixelsPerBlock;
    }
    public int getPixelH() {
        return this.height * this.pixelsPerBlock;
    }

    public int getPixelsPerBlock() {
        return this.pixelsPerBlock;
    }

    public void setPixelsPerBlock(int pixelsPerBlock) {
        int clamped = Math.max(16, Math.min(256, pixelsPerBlock));
        if (this.pixelsPerBlock == clamped) return;
        this.pixelsPerBlock = clamped;
        this.destroyBrowser();
        this.setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    public static void clientTick(Level level, BlockPos pos, BlockState state, BrowserMasterBlockEntity be) {
        if (level.isClientSide && !be.hasInitialized && !be.isLoading) {
            if (be.width > 0 && be.height > 0) {
                if (be.initTimer < INIT_DELAY) {
                    be.initTimer++;
                    return;
                }
                be.hasInitialized = true;
                be.loadBrowserAsync();
            }
        }
    }

    private void loadBrowserAsync() {
        if (level == null || !level.isClientSide) return;

        synchronized (browserLock) {
            if (this.browserSubprocess != null || this.isLoading) return;
            this.isLoading = true;
        }

        CompletableFuture.runAsync(() -> {
            try {
                int pW = getPixelW();
                int pH = getPixelH();
                IBrowserController newBrowser = BrowserAPI.getInstance().createBrowser(this.currentUrl, pW, pH, 60);
                BrowserRender newRender = newBrowser == null ? null : BrowserAPI.getInstance().GetBrowserRender(newBrowser);

                Minecraft.getInstance().execute(() -> {
                    synchronized (browserLock) {
                        if (this.isRemoved()) {
                            if (newRender != null) newRender.close();
                            if (newBrowser != null) BrowserAPI.getInstance().removeBrowser(newBrowser);
                            this.isLoading = false;
                            return;
                        }

                        this.browserSubprocess = newBrowser;
                        this.browserRender = newRender;
                        if (this.browserSubprocess != null) {
                            this.browserSubprocess.LoadUrl(this.currentUrl);
                            this.browserSubprocess.SetVolume((float) this.volume);
                        }
                    }
                    this.isLoading = false;
                    this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Minecraft.getInstance().execute(() -> this.isLoading = false);
            }
        });
    }

    public void setStructureInfo(int w, int h, int mRelX, int mRelY, Set<BlockPos> nodes) {
        boolean sizeChanged = (this.width != w || this.height != h);

        this.width = w;
        this.height = h;
        this.masterRelX = mRelX;
        this.masterRelY = mRelY;

        this.nodePositions.clear();
        if (nodes != null) this.nodePositions.addAll(nodes);

        if (sizeChanged) {
            this.destroyBrowser();
        } else if (this.browserSubprocess == null && !this.isLoading) {
            this.hasInitialized = false;
            this.initTimer = 0;
        }

        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public BrowserRender getBrowserRender() {
        return this.browserRender;
    }

    public int getBrowserTextureId() {
        BrowserRender render = this.browserRender;
        if (render == null) return -1;
        try {
            return render.render(getPixelW(), getPixelH());
        } catch (Throwable e) {
            return -1;
        }
    }

    public void handleClick(BrowserNodeBlockEntity nodeBe, BlockHitResult hit) {
        synchronized (browserLock) {
            if (browserSubprocess == null) return;
            try {
                Direction facing = this.getBlockState().getValue(BrowserMasterBlock.FACING);
                double dx = hit.getLocation().x - nodeBe.getBlockPos().getX();
                double dy = hit.getLocation().y - nodeBe.getBlockPos().getY();
                double dz = hit.getLocation().z - nodeBe.getBlockPos().getZ();

                double localHitX;
                switch (facing) {
                    case SOUTH -> localHitX = dx;
                    case NORTH -> localHitX = 1.0 - dx;
                    case WEST -> localHitX = dz;
                    case EAST -> localHitX = 1.0 - dz;
                    default -> {
                        return;
                    }
                }

                double globalX = nodeBe.getRelX() + localHitX;
                double globalYFromTop = height - (nodeBe.getRelY() + dy);
                int browserX = (int) ((globalX / width) * getPixelW());
                int browserY = (int) ((globalYFromTop / height) * getPixelH());

                browserSubprocess.SendMouseClick(browserX, browserY, 0, false);
                browserSubprocess.SendMouseClick(browserX, browserY, 0, true);
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.width = tag.getInt("W");
        this.height = tag.getInt("H");
        this.masterRelX = tag.getInt("mRX");
        this.masterRelY = tag.getInt("mRY");
        String oldUrl = this.currentUrl;
        double oldVol = this.volume;
        int oldScale = this.pixelsPerBlock;
        this.currentUrl = tag.getString("Url");
        this.volume = tag.contains("Vol") ? tag.getDouble("Vol") : 1.0;
        this.pixelsPerBlock = Math.max(16, Math.min(256, tag.contains("Scale") ? tag.getInt("Scale") : 64));
        this.owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        if (this.level != null && this.level.isClientSide) {
            if (oldScale != this.pixelsPerBlock) {
                this.destroyBrowser();
            }
            synchronized (browserLock) {
                if (browserSubprocess != null) {
                    if (!this.currentUrl.equals(oldUrl)) {
                        browserSubprocess.LoadUrl(this.currentUrl);
                    }
                    if (this.volume != oldVol) {
                        browserSubprocess.SetVolume((float) this.volume);
                    }
                }
            }
        }

    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("W", width);
        tag.putInt("H", height);
        tag.putInt("mRX", masterRelX);
        tag.putInt("mRY", masterRelY);
        tag.putString("Url", currentUrl);
        tag.putDouble("Vol", volume);
        tag.putInt("Scale", pixelsPerBlock);
        if (owner != null) tag.putUUID("Owner", owner);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMasterRelX() {
        return masterRelX;
    }

    public int getMasterRelY() {
        return masterRelY;
    }

    public String getUrl() {
        return currentUrl;
    }

    public void setUrl(String url) {
        this.currentUrl = url;
        synchronized (browserLock) {
            if (browserSubprocess != null) browserSubprocess.LoadUrl(url);
        }
        this.setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void setVolume(double volume) {
        this.volume = volume;
        synchronized (browserLock) {
            if (browserSubprocess != null) browserSubprocess.SetVolume((float) volume);
        }
        this.setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void destroyBrowser() {
        synchronized (browserLock) {
            if (this.browserSubprocess != null) {
                if (this.browserRender != null) {
                    try {
                        this.browserRender.close();
                    } catch (Throwable ignored) {}
                    this.browserRender = null;
                }
                BrowserAPI.getInstance().removeBrowser(this.browserSubprocess);
                this.browserSubprocess = null;
            }
        }
        this.isLoading = false;
        this.hasInitialized = false;
        this.initTimer = 0;
    }

    @Override
    public void setRemoved() {
        destroyBrowser();
        super.setRemoved();
    }


    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(width + 1, height + 1, width + 1);
    }
}