package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class SaturationHUD extends Feature {

    public SaturationHUD() {
        super("saturationhud", "Saturation HUD", Category.HUD, false);
        setHudPositionable(680, 380);
    }

    @Override
    public void renderHud(GuiGraphicsExtractor ctx, Font font, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || dev.wraith.WraithModClient.isScreenOpen) return;
        int sat = (int) mc.player.getFoodData().getSaturationLevel();
        String text = "Sat: " + sat;
        int color = sat > 10 ? 0xFFFFD700 : sat > 4 ? 0xFFFFAA00 : 0xFFFF5555;
        ctx.text(font, text, getHudX(), getHudY(), color, true);
    }

    @Override public int hudBoxW() { return 55; }
    @Override public int hudBoxH() { return 10; }
}
