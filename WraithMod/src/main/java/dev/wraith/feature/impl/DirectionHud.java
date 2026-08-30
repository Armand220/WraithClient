package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class DirectionHud extends Feature {

    private static final String[] DIRS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

    public DirectionHud() {
        super("direction", "Direction", Category.HUD, false);
        setHudPositionable(4, 170);
    }

    @Override
    public void renderHud(DrawContext ctx, TextRenderer tr, int sw, int sh) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        float yaw = client.player.getYaw() % 360;
        if (yaw < 0) yaw += 360;
        int idx = (int) ((yaw + 22.5f) / 45f) % 8;

        String text = DIRS[idx] + " (" + Math.round(yaw) + "°)";
        ctx.drawTextWithShadow(tr, text, getHudX(), getHudY(), 0xFFAAAAAA);
    }

    @Override public int hudBoxW() { return 80; }
    @Override public int hudBoxH() { return 10; }
}
