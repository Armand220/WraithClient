package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;

public class PotionHUD extends Feature {
    public PotionHUD() { super("potionhud", "Potion HUD", Category.HUD, true); }

    @Override
    public void renderHud(GuiGraphics g, Font font, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        List<MobEffectInstance> effects = new ArrayList<>(mc.player.getActiveEffects());
        if (effects.isEmpty()) return;
        int y = sh / 2 - (effects.size() * 11) / 2;
        for (MobEffectInstance e : effects) {
            String name = e.getEffect().value().getDescriptionId();
            String amp = e.getAmplifier() > 0 ? " " + toRoman(e.getAmplifier() + 1) : "";
            String dur = e.isInfinite() ? "inf" : formatTicks(e.getDuration());
            String line = name + amp + " " + dur;
            g.drawString(font, line, sw - font.width(line) - 4, y, 0xFFFFFFFF);
            y += 11;
        }
    }

    private static String formatTicks(int t) {
        int s = t / 20;
        return s >= 60 ? (s / 60) + "m" + String.format("%02d", s % 60) + "s" : s + "s";
    }

    private static String toRoman(int n) {
        return switch (n) { case 2->"II"; case 3->"III"; case 4->"IV"; case 5->"V"; default->String.valueOf(n); };
    }
}
