package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayDeque;
import java.util.Deque;

public class CpsCounter extends Feature {
    private final Deque<Long> left  = new ArrayDeque<>();
    private final Deque<Long> right = new ArrayDeque<>();
    private boolean lastLeft, lastRight;

    public CpsCounter() { super("cps", "CPS Counter", Category.HUD, true); }

    @Override
    public void tick(Minecraft mc) {
        long now = System.currentTimeMillis();
        boolean l = mc.options.keyAttack.isDown();
        boolean r = mc.options.keyUse.isDown();
        if (l && !lastLeft)  left.addLast(now);
        if (r && !lastRight) right.addLast(now);
        lastLeft = l; lastRight = r;
        long cut = now - 1000L;
        while (!left.isEmpty()  && left.peekFirst()  < cut) left.pollFirst();
        while (!right.isEmpty() && right.peekFirst() < cut) right.pollFirst();
    }

    @Override
    public void renderHud(GuiGraphics g, Font font, int sw, int sh) {
        g.drawString(font, left.size() + " | " + right.size() + " CPS", 4, 14, 0xFFFFFFFF);
    }
}
