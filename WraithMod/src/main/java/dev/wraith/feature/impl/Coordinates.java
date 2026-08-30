package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class Coordinates extends Feature {

    public Coordinates() {
        super("coords", "Coordinates", Category.HUD, true);
        setHudPositionable(4, 160);
    }

    @Override
    public void renderHud(DrawContext ctx, TextRenderer tr, int sw, int sh) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();

        String line = String.format("X: %.1f  Y: %.1f  Z: %.1f", x, y, z);
        ctx.drawTextWithShadow(tr, line, getHudX(), getHudY(), 0xFFFFFFFF);
    }

    @Override public int hudBoxW() { return 130; }
    @Override public int hudBoxH() { return 10; }
}
