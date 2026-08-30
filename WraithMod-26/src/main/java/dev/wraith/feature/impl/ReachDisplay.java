package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ReachDisplay extends Feature {

    public ReachDisplay() {
        super("reachdisplay", "Reach Display", Category.COMBAT, false);
        setHudPositionable(340, 150);
    }

    @Override
    public void renderHud(GuiGraphicsExtractor ctx, Font font, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || dev.wraith.WraithModClient.isScreenOpen) return;
        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.ENTITY) return;
        Entity target = ((EntityHitResult) mc.hitResult).getEntity();
        Vec3 eye = mc.player.getEyePosition();
        double reach = eye.distanceTo(new Vec3(
            target.getX(), target.getY() + target.getBbHeight() / 2.0, target.getZ()));
        String text = String.format("Reach: %.2fb", reach);
        ctx.text(font, text, getHudX(), getHudY(), 0xFFFF5555, true);
    }

    @Override public int hudBoxW() { return 80; }
    @Override public int hudBoxH() { return 10; }
}
