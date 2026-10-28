package dev.whoteva.anglersedge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.whoteva.anglersedge.block.entity.FishTrophyBlockEntity;
import dev.whoteva.anglersedge.util.FishRarity;
import dev.whoteva.anglersedge.util.FishWeight;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
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
            renderPlainLabel(Component.translatable("gui.anglersedge.trophy_empty"), 0xFFAAAAAA, poseStack, bufferSource, packedLight);
            return;
        }

        float quality = FishWeight.qualityOf(displayed);
        FishRarity.Tier tier = FishRarity.tierOf(displayed.getItem());
        float scale = quality < 0f ? 0.9F : 0.72F + quality * 0.48F;
        if (tier == FishRarity.Tier.EPIC || tier == FishRarity.Tier.BOSS) {
            scale = Math.max(scale, 1.05F);
        }
        float time = (trophy.getLevel() == null ? 0 : trophy.getLevel().getGameTime()) + partialTick;
        float bob = Mth.sin(time / 10F) * 0.04F;

        poseStack.pushPose();
        // y stays above the opaque cube so the fish is not buried in the plaque model.
        poseStack.translate(0.5, 1.35 + bob, 0.5);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(time * 2F));
        poseStack.scale(scale, scale, scale);

        itemRenderer.renderStatic(displayed, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, bufferSource, trophy.getLevel(), 0);

        poseStack.popPose();

        renderLabel(displayed, tier, poseStack, bufferSource, packedLight);
        spawnTierParticles(trophy, tier, quality);
    }

    private void spawnTierParticles(FishTrophyBlockEntity trophy, FishRarity.Tier tier, float quality) {
        if (trophy.getLevel() == null || trophy.getLevel().getGameTime() % 8 != 0) {
            return;
        }
        boolean sparkle = tier == FishRarity.Tier.EPIC || tier == FishRarity.Tier.BOSS || quality >= 0.97F;
        if (!sparkle) {
            return;
        }
        double x = trophy.getBlockPos().getX() + 0.5;
        double y = trophy.getBlockPos().getY() + 1.55;
        double z = trophy.getBlockPos().getZ() + 0.5;
        trophy.getLevel().addParticle(ParticleTypes.END_ROD, x, y, z,
                (trophy.getLevel().getRandom().nextDouble() - 0.5) * 0.02,
                0.01,
                (trophy.getLevel().getRandom().nextDouble() - 0.5) * 0.02);
    }

    private void renderPlainLabel(Component text, int color, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1.35, 0.5);
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        poseStack.scale(0.02F, -0.02F, 0.02F);
        Matrix4f matrix = poseStack.last().pose();
        float width = font.width(text);
        font.drawInBatch(text, -width / 2F, 0, color, false, matrix, bufferSource,
                Font.DisplayMode.NORMAL, 0x40000000, packedLight);
        poseStack.popPose();
    }

    private void renderLabel(ItemStack displayed, FishRarity.Tier tier, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
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
        int backdrop = switch (tier) {
            case UNCOMMON -> 0x40205030;
            case RARE -> 0x40203060;
            case EPIC -> 0x40302050;
            case BOSS -> 0x40504020;
            case null, default -> 0x40000000;
        };
        font.drawInBatch(name, -nameWidth / 2F, 0, 0xFFFFFF, false, matrix, bufferSource,
                Font.DisplayMode.NORMAL, backdrop, packedLight);

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
