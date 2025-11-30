package top.yunmouren.browserblock.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import top.yunmouren.browserblock.block.BrowserMasterBlock;
import top.yunmouren.browserblock.block.BrowserMasterBlockEntity;

public class BrowserMasterBlockRenderer implements BlockEntityRenderer<BrowserMasterBlockEntity> {

    public BrowserMasterBlockRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(BrowserMasterBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        int textureId = entity.getBrowserTextureId();
        if (textureId <= 0) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        BlockState state = entity.getBlockState();
        Direction facing = state.getOptionalValue(BrowserMasterBlock.FACING).orElse(Direction.NORTH);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

        poseStack.translate(0, 0, 0.501);

        int totalW = entity.getWidth();
        int totalH = entity.getHeight();
        double centerOffsetX = (totalW / 2.0) - 0.5;
        double centerOffsetY = (totalH / 2.0) - 0.5;
        poseStack.translate(centerOffsetX, centerOffsetY, 0);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, textureId);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        Matrix4f mat = poseStack.last().pose();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float halfWidth = totalW / 2f;
        float halfHeight = totalH / 2f;

        buffer.vertex(mat, -halfWidth, halfHeight, 0).uv(0, 0).endVertex();
        buffer.vertex(mat, -halfWidth, -halfHeight, 0).uv(0, 1).endVertex();
        buffer.vertex(mat, halfWidth, -halfHeight, 0).uv(1, 1).endVertex();
        buffer.vertex(mat, halfWidth, halfHeight, 0).uv(1, 0).endVertex();

        tesselator.end();

        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(BrowserMasterBlockEntity blockEntity) {
        return true;
    }
}