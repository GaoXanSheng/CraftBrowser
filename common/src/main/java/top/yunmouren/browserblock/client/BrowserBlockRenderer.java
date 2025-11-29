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
        // 1. Ensure we only render for the master block.
        BrowserBlockEntity master = entity.getMaster();
        if (master == null || !entity.getBlockPos().equals(master.getBlockPos())) {
            return;
        }

        // 2. Get the texture ID from the entity. This is now a lightweight call.
        int textureId = master.getBrowserTextureId();
        if (textureId <= 0) {
            // If there's no valid texture, don't attempt to render.
            return;
        }

        // 3. Standard rendering setup.
        poseStack.pushPose();

        // Center the pose in the block.
        poseStack.translate(0.5, 0.5, 0.5);

        // Rotate the screen to face the correct direction.
        BlockState state = entity.getBlockState();
        Direction facing = state.getOptionalValue(BrowserBlock.FACING).orElse(Direction.NORTH);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

        // Move the screen slightly forward to avoid Z-fighting.
        poseStack.translate(0, 0, 0.501);

        // Adjust the screen's position to be centered relative to the multi-block structure.
        int totalW = master.getWidth();
        int totalH = master.getHeight();
        double centerOffsetX = (totalW / 2.0) - (master.getRelX() + 0.5);
        double centerOffsetY = (totalH / 2.0) - (master.getRelY() + 0.5);
        poseStack.translate(centerOffsetX, centerOffsetY, 0);

        // 4. Set up the rendering pipeline for a textured quad.
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

        // Define the quad vertices and their UV coordinates.
        buffer.vertex(mat, -halfWidth, halfHeight, 0).uv(0, 0).endVertex();   // Top-left
        buffer.vertex(mat, -halfWidth, -halfHeight, 0).uv(0, 1).endVertex();  // Bottom-left
        buffer.vertex(mat, halfWidth, -halfHeight, 0).uv(1, 1).endVertex();   // Bottom-right
        buffer.vertex(mat, halfWidth, halfHeight, 0).uv(1, 0).endVertex();    // Top-right

        tesselator.end();

        // 5. Clean up the render state.
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(BrowserBlockEntity blockEntity) {
        // Render even when off-screen to keep the browser content updated.
        return true;
    }
}
