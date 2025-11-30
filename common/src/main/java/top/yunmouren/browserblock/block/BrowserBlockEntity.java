package top.yunmouren.browserblock.block;

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
import org.jetbrains.annotations.Nullable;
import top.yunmouren.browserblock.ModBlocks;
import top.yunmouren.craftbrowser.client.browser.api.BrowserAPI;
import top.yunmouren.craftbrowser.client.browser.api.BrowserSubprocess;
import top.yunmouren.craftbrowser.client.config.Config;
import java.util.UUID;

public class BrowserBlockEntity extends BlockEntity {

    // 【修改点 1】: 默认 Master 位置为 null，表示它是未绑定的节点
    @Nullable
    private BlockPos masterPos = null;

    private int relX = 0, relY = 0;
    private int width = 1, height = 1;

    private String currentUrl = Config.CLIENT.customURL.get();
    private String browserId;

    private boolean isInitialized = false;
    private int initTimer = 0;
    private static final int INIT_DELAY_TICKS = 60;

    @Nullable
    private volatile BrowserSubprocess browserSubprocess;

    public BrowserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BROWSER_BLOCK_ENTITY.get(), pos, state);
        this.browserId = "B" + ((pos.getX() & 0xFF) << 16 | (pos.getY() & 0xFF) << 8 | (pos.getZ() & 0xFF));
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BrowserBlockEntity be) {
        // 只有确认为 Master 的节点才执行 Tick 逻辑
        if (!be.isMaster()) return;

        if (!be.isInitialized) {
            be.initTimer++;
            if (be.initTimer >= INIT_DELAY_TICKS) {
                be.loadBrowserAsync();
                be.isInitialized = true;
            }
        }
    }

    public void markForInitialization() {
        this.isInitialized = false;
        this.initTimer = 0;
    }

    private void loadBrowserAsync() {
        if (level == null || !level.isClientSide) return;
        if (this.browserSubprocess != null) return;

        destroyBrowser();

        int pixelW = this.width * 64;
        int pixelH = this.height * 64;
        String id = this.browserId;

        BrowserAPI.createBrowserAsync(id, this.currentUrl, pixelW, pixelH, 60, (subprocess) -> {
            if (this.isRemoved()) {
                BrowserAPI.removeBrowser(id);
                return;
            }
            this.browserSubprocess = subprocess;
        });
    }

    public void destroyBrowser() {
        if (level != null && level.isClientSide) {
            BrowserAPI.removeBrowser(this.browserId);
            this.browserSubprocess = null;
        }
    }
    public AABB getRenderBoundingBox() {
        return new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && level.isClientSide && isMaster()) {
            destroyBrowser();
        }
    }

    // --- 数据同步与结构管理 ---

    public void setMasterPos(BlockPos pos) {
        this.masterPos = pos;
        this.setChanged();
    }

    public void setStructureInfo(BlockPos masterPos, int w, int h, int rx, int ry) {
        this.masterPos = masterPos;
        this.width = w;
        this.height = h;
        this.relX = rx;
        this.relY = ry;
        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        int oldWidth = this.width;
        int oldHeight = this.height;
        String oldId = this.browserId;

        // 【修改点 3】: 读取 Master 坐标，如果没有相关 Key，保持 masterPos 为 null
        if (tag.contains("MX")) {
            masterPos = new BlockPos(tag.getInt("MX"), tag.getInt("MY"), tag.getInt("MZ"));
        } else {
            masterPos = null;
        }

        width = tag.getInt("W");
        height = tag.getInt("H");
        relX = tag.getInt("RX");
        relY = tag.getInt("RY");

        if (tag.contains("BID")) browserId = tag.getString("BID");
        if (tag.contains("Url")) currentUrl = tag.getString("Url");

        if (level != null && level.isClientSide) {
            // 如果 ID 变了 (换了 Master)，强制重新初始化
            if (oldId != null && !oldId.equals(browserId)) {
                this.isInitialized = false;
                this.initTimer = 0;
                this.browserSubprocess = null;
            }
            // 如果 ID 没变，但尺寸变了，且浏览器正在运行 -> Resize
            else if (browserSubprocess != null && (oldWidth != width || oldHeight != height)) {
                browserSubprocess.getPageHandler().resizeViewport(width * 64, height * 64);
            }
            // 如果还没初始化，重置计时器
            else if (browserSubprocess == null && !this.isInitialized) {
                this.initTimer = 0;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        // 【修改点 4】: 仅当 masterPos 存在时写入坐标
        if (masterPos != null) {
            tag.putInt("MX", masterPos.getX());
            tag.putInt("MY", masterPos.getY());
            tag.putInt("MZ", masterPos.getZ());
        }
        tag.putInt("W", width);
        tag.putInt("H", height);
        tag.putInt("RX", relX);
        tag.putInt("RY", relY);
        tag.putString("BID", browserId);
        tag.putString("Url", currentUrl);
    }

    // 【修改点 5】: 判断 Master 的逻辑变更
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

    public int getBrowserTextureId() {
        if (!isMaster()) {
            BrowserBlockEntity master = getMaster();
            return master != null ? master.getBrowserTextureId() : -1;
        }
        if (browserSubprocess != null) {
            return browserSubprocess.getRender(width * 64, height * 64);
        }
        return -1;
    }

    public void sendClickInput(int x, int y) {
        if (browserSubprocess != null) {
            browserSubprocess.getMouseHandler().mousePress(x, y, 0);
            browserSubprocess.getMouseHandler().mouseRelease(x, y, 0);
        }
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
    public int getRelX() { return relX; }
    public int getRelY() { return relY; }
    public String getUrl() { return currentUrl; }
    public void setUrl(String url) {
        if(browserSubprocess != null) browserSubprocess.getPageHandler().loadUrl(url);
        this.currentUrl = url;
        this.setChanged();

    }
}