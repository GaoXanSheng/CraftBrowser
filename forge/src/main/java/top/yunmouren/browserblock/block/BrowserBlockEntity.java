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
import top.yunmouren.craftbrowser.client.config.Config;
import top.yunmouren.craftbrowser.proxy.CommonProxy;

public class BrowserBlockEntity extends BlockEntity {

    private BlockPos masterPos;
    private int relX = 0;
    private int relY = 0;
    private int width = 1;
    private int height = 1;

    // 1. 新增：存储当前 URL，默认值为 Google 或你的主页
    private String currentUrl = Config.CLIENT.customURL.get();
    // 标记是否已经加载过初始页面
    private boolean isInitialized = false;

    private int lastPixelWidth = -1;
    private int lastPixelHeight = -1;

    public BrowserBlockEntity(BlockPos pos, BlockState state) {
        super(CommonProxy.BROWSER_BLOCK_ENTITY.get(), pos, state);
    }

    public void setStructureData(BlockPos masterPos, int x, int y, int w, int h) {
        this.masterPos = masterPos;
        this.relX = x;
        this.relY = y;
        this.width = w;
        this.height = h;
        setChanged();
    }

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
        if (isMaster()) {
            if (this.width <= 0 || this.height <= 0) {
                return -1;
            }

            int pixelWidth = this.width * 64;
            int pixelHeight = this.height * 64;

            // 调整视口大小
            if (pixelWidth != lastPixelWidth || pixelHeight != lastPixelHeight) {
                BrowserAPI.getInstance().getManager().resizeViewport(pixelWidth, pixelHeight);
                this.lastPixelWidth = pixelWidth;
                this.lastPixelHeight = pixelHeight;
            }

            // 2. 新增：如果是第一次渲染且 URL 未加载，则加载 URL
            if (!isInitialized && level != null && level.isClientSide) {
                BrowserAPI.getInstance().getManager().loadUrl(this.currentUrl);
                isInitialized = true;
            }

            return BrowserAPI.getInstance().getRender().render(pixelWidth, pixelHeight);

        } else {
            BrowserBlockEntity master = getMaster();
            return master != null ? master.getBrowserTextureId() : -1;
        }
    }

    /**
     * 设置新的 URL 并同步
     */
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

        // 如果是在客户端，立即加载
        if (level != null && level.isClientSide) {
            BrowserAPI.getInstance().getManager().loadUrl(newUrl);
        }
    }

    /**
     * 获取当前 URL
     */
    public String getUrl() {
        if (!isMaster()) {
            BrowserBlockEntity master = getMaster();
            return master != null ? master.getUrl() : "";
        }
        return this.currentUrl;
    }

    public void sendClickInput(int x, int y) {
        if (isMaster()) {
            BrowserAPI.getInstance().getManager().mousePress(x, y, 0);
            BrowserAPI.getInstance().getManager().mouseRelease(x, y, 0);
        } else {
            BrowserBlockEntity master = getMaster();
            if (master != null) master.sendClickInput(x, y);
        }
    }

    public int getRelX() { return relX; }
    public int getRelY() { return relY; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    // === NBT / Data Handling ===

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
        // 3. 保存 URL
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
        // 4. 读取 URL
        if (tag.contains("BrowserUrl")) {
            this.currentUrl = tag.getString("BrowserUrl");
            // 读取 NBT 后，如果是在客户端，可能需要重新加载页面
            // 但通常 load() 是在数据包同步时调用的，所以我们可以标记需要刷新
            this.isInitialized = false;
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        // 建议使用无限包围盒防止视锥剔除导致黑屏
        if (this.isMaster()) {
            return new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }
        return super.getRenderBoundingBox();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        // 确保同步数据包含 URL
        CompoundTag tag = saveWithoutMetadata();
        tag.putString("BrowserUrl", currentUrl);
        return tag;
    }
}