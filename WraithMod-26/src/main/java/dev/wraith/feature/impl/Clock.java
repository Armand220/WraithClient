package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Clock extends Feature {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("h:mm a");

    public Clock() {
        super("clock", "Clock", Category.HUD, false);
        setHudPositionable(700, 16);
    }

    @Override
    public void renderHud(GuiGraphicsExtractor ctx, Font font, int sw, int sh) {
        if (dev.wraith.WraithModClient.isScreenOpen) return;
        String time = LocalTime.now().format(FMT);
        ctx.text(font, time, getHudX(), getHudY(), 0xFFAAAAAA, true);
    }

    @Override public int hudBoxW() { return 55; }
    @Override public int hudBoxH() { return 10; }
}
