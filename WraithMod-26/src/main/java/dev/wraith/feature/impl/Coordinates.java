package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class Coordinates extends Feature {

    public Coordinates() {
        super("coords", "Coordinates", Category.HUD, true);
        setHudPositionable(4, 160);
    }

    @Override
    public void renderHud(GuiGraphicsExtractor ctx, Font font, int sw, int sh) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();
        String line = String.format("X: %.1f  Y: %.1f  Z: %.1f", x, y, z);
        ctx.text(font, line, getHudX(), getHudY(), 0xFFFFFFFF, true);
    }

    @Override public int hudBoxW() { return 130; }
    @Override public int hudBoxH() { return 10; }
}
