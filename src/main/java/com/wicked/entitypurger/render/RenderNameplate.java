package com.wicked.entitypurger.render;

import com.wicked.entitypurger.entity.EntityNameplate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;

import javax.annotation.Nullable;

public class RenderNameplate extends Render<EntityNameplate> {

    public RenderNameplate(RenderManager renderManager) {
        super(renderManager);
    }

    public void doRender(EntityNameplate entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.pushMatrix();

        GlStateManager.translate((float)x, (float)y, (float)z);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);

        Vec2f pitchAndYaw = Minecraft.getMinecraft().getRenderViewEntity().getPitchYaw();

        GlStateManager.rotate(-pitchAndYaw.y, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(pitchAndYaw.x, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);

        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();

        FontRenderer fontRenderer = getFontRendererFromRenderManager();
        String nameplateStr = entity.getNameplateFor().getClass().getCanonicalName();
        int width = fontRenderer.getStringWidth(nameplateStr) / 2;


        GlStateManager.disableDepth();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos((-width - 3), -2D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferbuilder.pos((-width - 3), 9D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferbuilder.pos((width + 3), 9D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferbuilder.pos((width + 3), -2D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.5F).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        fontRenderer.drawString(nameplateStr, -fontRenderer.getStringWidth(nameplateStr) / 2, 0, 16777215);
        GlStateManager.enableDepth();

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityNameplate entity) {
        return null;
    }
}
