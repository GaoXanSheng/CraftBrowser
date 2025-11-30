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
import top.yunmouren.craftbrowser.client.browser.api.BrowserAPI;
import top.yunmouren.craftbrowser.client.browser.api.BrowserSubprocess;
import top.yunmouren.craftbrowser.client.config.Config;

import static top.yunmouren.browserblock.ModBlocks.BROWSER_BLOCK_ENTITY;

public class BrowserBlockEntity extends BlockEntity {

    private BlockPos masterPos;
    private int relX = 0;
    private int relY = 0;
    private int width = 1;
    private int height = 1;
    private String currentUrl = Config.CLIENT.customURL.get();

    // 唯一标识符，基于坐标生成
    private String spoutId;

    // 防止重复请求的加载锁
    private boolean isLoading = false;

    @Nullable
    private volatile BrowserSubprocess browserSubprocess;

    public BrowserBlockEntity(BlockPos pos, BlockState state) {
        super(BROWSER_BLOCK_ENTITY.get(), pos, state);
    }

    /**
     * 获取 SpoutID，如果未初始化则初始化
     */
    private String getSpoutId() {
        if (this.spoutId == null) {
            this.spoutId = "browserblock-" + this.worldPosition.getX() + "-" + this.worldPosition.getY() + "-" + this.worldPosition.getZ();
        }
        return this.spoutId;
    }

    /**
     * 获取浏览器实例（异步懒加载）
     */
    @Nullable
    private BrowserSubprocess getBrowserSubprocess() {
        // 仅在客户端且为 Master 时运行
        if (this.level == null || !this.level.isClientSide || !isMaster()) {
            return null;
        }

        // 1. 如果已有实例，直接返回
        if (this.browserSubprocess != null) {
            return this.browserSubprocess;
        }

        // 2. 如果正在加载中，返回 null (渲染器应处理这种情况)
        if (this.isLoading) {
            return null;
        }

        // 3. 开始异步加载
        this.isLoading = true;
        int pixelWidth = this.width * 64;
        int pixelHeight = this.height * 64;
        String id = getSpoutId();

        // 调用异步创建 API
        BrowserAPI.createBrowserAsync(id, this.currentUrl, pixelWidth, pixelHeight, 60, (subprocess) -> {
            // 检查方块是否已被移除
            if (this.isRemoved()) {
                BrowserAPI.removeBrowser(id);
                this.isLoading = false;
                return;
            }
            // 赋值实例
            this.browserSubprocess = subprocess;

            // 初始化视口大小
            if (this.browserSubprocess != null) {
                this.browserSubprocess.getPageHandler().resizeViewport(pixelWidth, pixelHeight);
            }

            // 解除锁
            this.isLoading = false;
        });

        // 在回调完成前返回 null
        return null;
    }

    public void onLoad() {
        if (level != null && level.isClientSide && isMaster()) {
            getBrowserSubprocess(); // 触发预加载
        }
    }

    public int getBrowserTextureId() {
        int pixelWidth = this.width * 64;
        int pixelHeight = this.height * 64;

        if (!isMaster()) {
            BrowserBlockEntity master = getMaster();
            return master != null ? master.getBrowserTextureId() : -1;
        }

        // 尝试获取实例
        BrowserSubprocess subprocess = getBrowserSubprocess();

        // 如果实例存在且渲染器就绪，返回纹理ID
        if (subprocess != null) {
            return subprocess.getRender(pixelWidth, pixelHeight);
        }

        // 正在加载或失败返回 -1
        return -1;
    }

    // --- Block Entity Lifecycle ---

    @Override
    public void setRemoved() {
        super.setRemoved();
        // 方块移除时清理浏览器资源
        if (level != null && level.isClientSide) {
            if (browserSubprocess != null) {
                BrowserAPI.removeBrowser(getSpoutId());
                browserSubprocess = null;
            }
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

        // 如果结构大小改变，调整浏览器视口
        if (isMaster() && level != null && level.isClientSide) {
            BrowserSubprocess subprocess = getBrowserSubprocess(); // 不会触发新加载，只获取现有
            if (subprocess != null) {
                subprocess.getPageHandler().resizeViewport(w * 64, h * 64);
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

        // 客户端直接导航
        if (level != null && level.isClientSide) {
            BrowserSubprocess subprocess = getBrowserSubprocess();
            if (subprocess != null) {
                subprocess.getPageHandler().loadUrl(newUrl);
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
            subprocess.getMouseHandler().mousePress(x,y,0);
            subprocess.getMouseHandler().mouseRelease(x,y,0);
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

    public int getRelX() { return relX; }
    public int getRelY() { return relY; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

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