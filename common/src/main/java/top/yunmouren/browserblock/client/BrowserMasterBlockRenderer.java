package top.yunmouren.browserblock.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import top.yunmouren.browserblock.block.BrowserMasterBlock;
import top.yunmouren.browserblock.block.BrowserMasterBlockEntity;

public class BrowserMasterBlockRenderer implements BlockEntityRenderer<BrowserMasterBlockEntity> {

    public BrowserMasterBlockRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(BrowserMasterBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ResourceLocation textureId = entity.getBrowserTextureId();
        if (textureId == null || textureId.equals(ResourceLocation.withDefaultNamespace(""))) {
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
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f mat = poseStack.last().pose();

        float halfWidth = totalW / 2f;
        float halfHeight = totalH / 2f;

        buffer.addVertex(mat, -halfWidth, halfHeight, 0).setUv(0, 0);
        buffer.addVertex(mat, -halfWidth, -halfHeight, 0).setUv(0, 1);
        buffer.addVertex(mat, halfWidth, -halfHeight, 0).setUv(1, 1);
        buffer.addVertex(mat, halfWidth, halfHeight, 0).setUv(1, 0);

        MeshData meshData = buffer.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }

        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(BrowserMasterBlockEntity blockEntity) {
        return true;
    }
}