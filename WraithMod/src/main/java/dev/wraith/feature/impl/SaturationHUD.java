package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.HungerManager;

public class SaturationHUD extends Feature {

    public SaturationHUD() {
        super("saturationhud", "Saturation HUD", Category.HUD, false);
        setHudPositionable(680, 380);
    }

    @Override
    public void renderHud(DrawContext ctx, TextRenderer tr, int sw, int sh) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.currentScreen != null) return;

        HungerManager hunger = mc.player.getHungerManager();
        int sat = (int) hunger.getSaturationLevel();
        String text = "Sat: " + sat;

        int color = sat > 10 ? 0xFFFFD700 : sat > 4 ? 0xFFFFAA00 : 0xFFFF5555;
        ctx.drawTextWithShadow(tr, text, getHudX(), getHudY(), color);
    }

    @Override public int hudBoxW() { return 55; }
    @Override public int hudBoxH() { return 10; }
}
