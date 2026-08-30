package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;

public class PotionHUD extends Feature {

    public PotionHUD() {
        super("potionhud", "Potion HUD", Category.HUD, true);
        setHudPositionable(660, 100);
    }

    @Override
    public void renderHud(GuiGraphicsExtractor ctx, Font font, int sw, int sh) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        List<MobEffectInstance> effects = new ArrayList<>(client.player.getActiveEffects());
        if (effects.isEmpty()) return;
        int y = getHudY();
        for (MobEffectInstance effect : effects) {
            String name = Component.translatable(
                effect.getEffect().value().getDescriptionId()).getString();
            int amp = effect.getAmplifier();
            String ampStr = amp > 0 ? " " + toRoman(amp + 1) : "";
            String duration = effect.isInfiniteDuration() ? "inf" : formatTicks(effect.getDuration());
            String line = name + ampStr + " §7" + duration;
            ctx.text(font, line, getHudX(), y, 0xFFFFFFFF, true);
            y += 11;
        }
    }

    private static String formatTicks(int ticks) {
        int secs = ticks / 20;
        return secs >= 60
            ? (secs / 60) + "m" + String.format("%02d", secs % 60) + "s"
            : secs + "s";
    }

    private static String toRoman(int n) {
        return switch (n) {
            case 2  -> "II";
            case 3  -> "III";
            case 4  -> "IV";
            case 5  -> "V";
            default -> String.valueOf(n);
        };
    }

    @Override public int hudBoxW() { return 120; }
    @Override public int hudBoxH() { return 55; }
}
