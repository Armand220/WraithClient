package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class PotionHUD extends Feature {

    public PotionHUD() {
        super("potionhud", "Potion HUD", Category.HUD, true);
    }

    @Override
    public void renderHud(MatrixStack stack, TextRenderer tr, int sw, int sh) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        List<StatusEffectInstance> effects = new ArrayList<>(client.player.getStatusEffects());
        if (effects.isEmpty()) return;

        int y = sh / 2 - (effects.size() * 11) / 2;

        for (StatusEffectInstance effect : effects) {
            String name = Text.translatable(effect.getTranslationKey()).getString();
            int amp   = effect.getAmplifier();
            String ampStr = amp > 0 ? " " + toRoman(amp + 1) : "";
            String duration = effect.isInfinite() ? "inf" : formatTicks(effect.getDuration());
            String line = name + ampStr + " §7" + duration;
            tr.drawWithShadow(stack, line, sw - tr.getWidth(line) - 4, y, 0xFFFFFFFF);
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
}
