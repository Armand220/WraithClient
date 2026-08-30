package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

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
    public void tick(Minecraft client) {
        long now = System.currentTimeMillis();
        boolean left  = client.options.keyAttack.isDown();
        boolean right = client.options.keyUse.isDown();
        if (left  && !lastLeft)  leftClicks.addLast(now);
        if (right && !lastRight) rightClicks.addLast(now);
        lastLeft  = left;
        lastRight = right;
        long cutoff = now - 1000L;
        while (!leftClicks.isEmpty()  && leftClicks.peekFirst()  < cutoff) leftClicks.pollFirst();
        while (!rightClicks.isEmpty() && rightClicks.peekFirst() < cutoff) rightClicks.pollFirst();
    }

    @Override
    public void renderHud(GuiGraphicsExtractor ctx, Font font, int sw, int sh) {
        String text = leftClicks.size() + " | " + rightClicks.size() + " CPS";
        ctx.text(font, text, getHudX(), getHudY(), 0xFFFFFFFF, true);
    }

    @Override public int hudBoxW() { return 72; }
    @Override public int hudBoxH() { return 10; }
}
