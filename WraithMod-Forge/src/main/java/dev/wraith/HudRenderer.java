package dev.wraith;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HudRenderer {

    private static final Minecraft MC = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (MC.gameSettings.showDebugInfo) return;
        if (MC.currentScreen != null && !(MC.currentScreen instanceof WraithMenuScreen)) return;

        FontRenderer fr = MC.fontRenderer;
        EntityPlayer player = MC.player;
        int y = 2;

        if (FeatureRegistry.FPS.isEnabled()) {
            String fps = "FPS: " + Minecraft.getDebugFPS();
            MC.fontRenderer.drawStringWithShadow(fps, 2, y, 0xFFFFFF);
            y += 10;
        }

        if (FeatureRegistry.COORDS.isEnabled() && player != null) {
            String coords = String.format("X: %.1f  Y: %.1f  Z: %.1f",
                player.posX, player.posY, player.posZ);
            fr.drawStringWithShadow(coords, 2, y, 0xFFFFFF);
            y += 10;
        }

        if (FeatureRegistry.TOGGLE_SPRINT.isEnabled() && player != null) {
            KeyBinding.setKeyBindState(MC.gameSettings.keyBindSprint.getKeyCode(), true);
        }
    }
}
