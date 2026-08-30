package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayDeque;
import java.util.Deque;

public class CpsCounter extends Feature {

    private final Deque<Long> leftClicks  = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();
    private boolean lastLeft, lastRight;

    public CpsCounter() {
        super("cps", "CPS Counter", Category.HUD, true);
        setHudPositionable(4, 16);
    }

    @Override
    public void tick(MinecraftClient client) {
        long now = System.currentTimeMillis();

        boolean left  = client.options.attackKey.isPressed();
        boolean right = client.options.useKey.isPressed();

        if (left  && !lastLeft)  leftClicks.addLast(now);
        if (right && !lastRight) rightClicks.addLast(now);

        lastLeft  = left;
        lastRight = right;

        long cutoff = now - 1000L;
        while (!leftClicks.isEmpty()  && leftClicks.peekFirst()  < cutoff) leftClicks.pollFirst();
        while (!rightClicks.isEmpty() && rightClicks.peekFirst() < cutoff) rightClicks.pollFirst();
    }

    @Override
    public void renderHud(DrawContext ctx, TextRenderer tr, int sw, int sh) {
        String text = leftClicks.size() + " | " + rightClicks.size() + " CPS";
        ctx.drawTextWithShadow(tr, text, getHudX(), getHudY(), 0xFFFFFFFF);
    }

    @Override public int hudBoxW() { return 72; }
    @Override public int hudBoxH() { return 10; }
}
