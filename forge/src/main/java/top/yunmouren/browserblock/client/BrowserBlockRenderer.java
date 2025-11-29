package top.yunmouren.browserblock.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import top.yunmouren.browserblock.block.BrowserBlock;
import top.yunmouren.browserblock.block.BrowserBlockEntity;

public class BrowserBlockRenderer implements BlockEntityRenderer<BrowserBlockEntity> {

    public BrowserBlockRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(BrowserBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // 1. 只有 Master 负责渲染屏幕
        BrowserBlockEntity master = entity.getMaster();
        if (master == null || !entity.getBlockPos().equals(master.getBlockPos())) {
            return;
        }

        int textureId = master.getBrowserTextureId();
        if (textureId <= 0) return;

        int totalW = master.getWidth();
        int totalH = master.getHeight();

        int masterRelX = master.getRelX();
        int masterRelY = master.getRelY(); // 通常 master 都在最底下，relY=0

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);

        BlockState state = entity.getBlockState();
        Direction facing = state.hasProperty(BrowserBlock.FACING) ? state.getValue(BrowserBlock.FACING) : Direction.NORTH;
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

        poseStack.translate(0, 0, 0.501);

        double centerOffsetX = (totalW / 2.0) - (masterRelX + 0.5);
        double centerOffsetY = (totalH / 2.0) - (masterRelY + 0.5);

        poseStack.translate(centerOffsetX, centerOffsetY, 0);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, textureId);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        Matrix4f mat = poseStack.last().pose();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float hw = totalW / 2f;
        float hh = totalH / 2f;

        buffer.vertex(mat, -hw, hh, 0).uv(0, 0).endVertex();   // 左上
        buffer.vertex(mat, -hw, -hh, 0).uv(0, 1).endVertex();  // 左下
        buffer.vertex(mat, hw, -hh, 0).uv(1, 1).endVertex();   // 右下
        buffer.vertex(mat, hw, hh, 0).uv(1, 0).endVertex();    // 右上

        tesselator.end();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(BrowserBlockEntity blockEntity) {
        return true;
    }
}