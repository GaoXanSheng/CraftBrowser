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
import top.yunmouren.craftbrowser.client.browser.api.BrowserAPI;
import top.yunmouren.craftbrowser.client.browser.api.BrowserSubprocess;
import top.yunmouren.craftbrowser.client.config.Config;

import java.util.HashSet;
import java.util.Set;

public class BrowserMasterBlockEntity extends BlockEntity {
    private int width = 1;
    private int height = 1;
    private String currentUrl = Config.CLIENT.customURL.get();

    // ID 不需要保存，每次重建生成新的即可
    private String browserId;
    private final Object browserLock = new Object();

    @Nullable
    private BrowserSubprocess browserSubprocess;

    private boolean isLoading = false;
    private boolean hasInitialized = false;

    // 初始化延迟计数器
    private int initTimer = 0;
    private static final int INIT_DELAY = 20;

    private final Set<BlockPos> nodePositions = new HashSet<>();

    public BrowserMasterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BROWSER_MASTER_ENTITY.get(), pos, state);
        this.generateNewId();
    }

    private void generateNewId() {
        this.browserId = "B_" + worldPosition.getX() + "_" + worldPosition.getY() + "_" + worldPosition.getZ();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BrowserMasterBlockEntity be) {
        if (!be.hasInitialized && !be.isLoading) {
            // 只有当尺寸合法（大于0）才开始创建
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
            if (this.browserSubprocess != null) return;
        }

        this.isLoading = true;
        int pixelW = Math.max(64, this.width * 64);
        int pixelH = Math.max(64, this.height * 64);
        this.generateNewId();
        String id = this.browserId;

        BrowserAPI.createBrowserAsync(id, this.currentUrl, pixelW, pixelH, 60, (subprocess) -> {
            Minecraft.getInstance().execute(() -> {
                if (this.isRemoved()) {
                    BrowserAPI.removeBrowser(id);
                    this.isLoading = false;
                    return;
                }

                synchronized (browserLock) {
                    this.browserSubprocess = subprocess;
                    if (this.browserSubprocess != null) {
                        this.browserSubprocess.getPageHandler().loadUrl(this.currentUrl);
                    }
                }

                this.isLoading = false;
                if (this.level != null) {
                    this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
                }
            });
        });
    }

    public void destroyBrowser() {
        if (level != null && level.isClientSide) {
            String idToRemove = null;
            synchronized (browserLock) {
                if (this.browserSubprocess != null) {
                    this.browserSubprocess = null;
                    idToRemove = this.browserId;
                }
            }
            if (idToRemove != null) {
                BrowserAPI.removeBrowser(idToRemove);
            }
            this.isLoading = false;
            this.hasInitialized = false;
            this.initTimer = 0;
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        destroyBrowser();
    }

    public void onChunkUnloaded() {
        destroyBrowser();
    }

    public void setStructureInfo(int w, int h, Set<BlockPos> nodes) {
        boolean changed = (this.width != w || this.height != h);
        this.width = w;
        this.height = h;
        this.nodePositions.clear();
        if (nodes != null) {
            this.nodePositions.addAll(nodes);
        }
        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public int getBrowserTextureId() {
        synchronized (browserLock) {
            if (browserSubprocess != null) {
                try {
                    // 传入当前尺寸，渲染器内部会检测是否匹配
                    return browserSubprocess.getRender(width * 64, height * 64);
                } catch (Throwable e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(Math.max(width, height) + 1);
    }

    // 省略 handleClick (未改动) ...
    public void handleClick(BrowserNodeBlockEntity nodeBe, BlockHitResult hit, Direction facing) {
        synchronized (browserLock) {
            if (browserSubprocess == null) return;
            try {
                double dx = hit.getLocation().x - nodeBe.getBlockPos().getX();
                double dy = hit.getLocation().y - nodeBe.getBlockPos().getY();
                double dz = hit.getLocation().z - nodeBe.getBlockPos().getZ();
                double localHitX;
                switch (facing) {
                    case SOUTH: localHitX = dx; break;
                    case NORTH: localHitX = 1.0 - dx; break;
                    case WEST: localHitX = dz; break;
                    case EAST: localHitX = 1.0 - dz; break;
                    default: return;
                }
                int relX = nodeBe.getRelX();
                int relY = nodeBe.getRelY();
                double globalX = relX + localHitX;
                double globalYFromBottom = relY + dy;
                double globalYFromTop = height - globalYFromBottom;
                int pixelW = this.width * 64;
                int pixelH = this.height * 64;
                int browserX = (int) ((globalX / width) * pixelW);
                int browserY = (int) ((globalYFromTop / height) * pixelH);
                browserSubprocess.getMouseHandler().mousePress(browserX, browserY, 0);
                browserSubprocess.getMouseHandler().mouseRelease(browserX, browserY, 0);
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        int oldWidth = this.width;
        int oldHeight = this.height;
        width = tag.getInt("W");
        height = tag.getInt("H");

        if (tag.contains("Url")) {
            String newUrl = tag.getString("Url");
            if (!newUrl.equals(this.currentUrl)) {
                this.currentUrl = newUrl;
                if (level != null && level.isClientSide) {
                    synchronized (browserLock) {
                        if (browserSubprocess != null) {
                            browserSubprocess.getPageHandler().loadUrl(this.currentUrl);
                        }
                    }
                }
            }
        }
        if (level != null && level.isClientSide) {
            if (width != oldWidth || height != oldHeight) {
               destroyBrowser();
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("W", width);
        tag.putInt("H", height);
        tag.putString("Url", currentUrl);
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

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getUrl() { return currentUrl; }
    public void setUrl(String url) {
        this.currentUrl = url;
        if (level != null && level.isClientSide) {
            synchronized (browserLock) {
                if (browserSubprocess != null) browserSubprocess.getPageHandler().loadUrl(url);
            }
        }
        this.setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }
}