package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Clock extends Feature {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("h:mm a");

    public Clock() {
        super("clock", "Clock", Category.HUD, false);
        setHudPositionable(700, 16);
    }

    @Override
    public void renderHud(DrawContext ctx, TextRenderer tr, int sw, int sh) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen != null) return;
        String time = LocalTime.now().format(FMT);
        ctx.drawTextWithShadow(tr, time, getHudX(), getHudY(), 0xFFAAAAAA);
    }

    @Override public int hudBoxW() { return 55; }
    @Override public int hudBoxH() { return 10; }
}
