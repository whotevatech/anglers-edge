package dev.whoteva.anglersedge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.whoteva.anglersedge.block.entity.FishTrophyBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.joml.Matrix4f;

/**
 * Draws the mounted fish floating just above the block, slowly spinning, plus its name (size
 * title included, e.g. "Massive Cod") and weight as small billboarded text above it - the same
 * "always face the camera" trick vanilla uses for player/mob nametags.
 */
public class FishTrophyRenderer implements BlockEntityRenderer<FishTrophyBlockEntity> {

    private final ItemRenderer itemRenderer;
    private final Font font;

    public FishTrophyRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public void render(FishTrophyBlockEntity trophy, float partialTick, PoseStack poseStack,
                        MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack displayed = trophy.displayed();
        if (displayed.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        // y=1.45, not somewhere inside 0..1: the block's own model is a full opaque cube
        // (cube_all), so anything drawn at a height inside that range is entombed in solid
        // geometry and invisible from outside no matter how it's lit. Floating it just above the
        // top face is what actually makes it visible.
        poseStack.translate(0.5, 1.45, 0.5);
        float angle = (trophy.getLevel() == null ? 0 : trophy.getLevel().getGameTime() + partialTick) * 2F;
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));
        poseStack.scale(0.9F, 0.9F, 0.9F);

        itemRenderer.renderStatic(displayed, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, bufferSource, trophy.getLevel(), 0);

        poseStack.popPose();

        renderLabel(displayed, poseStack, bufferSource, packedLight);
    }

    private void renderLabel(ItemStack displayed, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.5, 2.15, 0.5);
        // Billboard: face whichever way the camera currently is, same as an entity's nametag.
        // Scale must be (+, -, +) to match vanilla's own nametag code exactly - a (-, -, +)
        // scale, which is what this had before, flips the text quads' winding the wrong way and
        // gets them backface-culled: drawn, but invisible from every angle. Caught by comparing
        // directly against EntityRenderer#renderNameTag's bytecode.
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        poseStack.scale(0.025F, -0.025F, 0.025F);
        Matrix4f matrix = poseStack.last().pose();

        Component name = displayed.getHoverName();
        float nameWidth = font.width(name);
        font.drawInBatch(name, -nameWidth / 2F, 0, 0xFFFFFF, false, matrix, bufferSource,
                Font.DisplayMode.NORMAL, 0x40000000, packedLight);

        ItemLore lore = displayed.get(DataComponents.LORE);
        if (lore != null) {
            int lineY = 10;
            for (Component line : lore.lines()) {
                float lineWidth = font.width(line);
                font.drawInBatch(line, -lineWidth / 2F, lineY, 0xAAAAAA, false, matrix, bufferSource,
                        Font.DisplayMode.NORMAL, 0x40000000, packedLight);
                lineY += 10;
            }
        }

        poseStack.popPose();
    }
}
