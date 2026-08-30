package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class ReachDisplay extends Feature {

    public ReachDisplay() {
        super("reachdisplay", "Reach Display", Category.COMBAT, false);
        setHudPositionable(340, 150);
    }

    @Override
    public void renderHud(DrawContext ctx, TextRenderer tr, int sw, int sh) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.targetedEntity == null) return;

        Entity target = client.targetedEntity;
        Vec3d eye = client.player.getEyePos();
        double reach = eye.distanceTo(new Vec3d(
            target.getX(), target.getY() + target.getHeight() / 2.0, target.getZ()));

        String text = String.format("Reach: %.2fb", reach);
        ctx.drawTextWithShadow(tr, text, getHudX(), getHudY(), 0xFFFF5555);
    }

    @Override public int hudBoxW() { return 80; }
    @Override public int hudBoxH() { return 10; }
}
