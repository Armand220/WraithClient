package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class Keystrokes extends Feature {
    public Keystrokes() { super("keystrokes", "Keystrokes", Category.HUD, true); }

    @Override
    public void renderHud(GuiGraphics g, Font font, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        int bw = 18, bh = 14, pad = 2;
        int ox = sw / 2 - bw - pad / 2;
        int oy = sh - 80;
        drawKey(g, font, "W",   ox,            oy,            bw, bh, mc.options.keyUp.isDown());
        drawKey(g, font, "A",   ox - bw - pad, oy + bh + pad, bw, bh, mc.options.keyLeft.isDown());
        drawKey(g, font, "S",   ox,            oy + bh + pad, bw, bh, mc.options.keyDown.isDown());
        drawKey(g, font, "D",   ox + bw + pad, oy + bh + pad, bw, bh, mc.options.keyRight.isDown());
        drawKey(g, font, "LMB", ox - bw - pad, oy - bh - pad, bw, bh, mc.options.keyAttack.isDown());
        drawKey(g, font, "RMB", ox + bw + pad, oy - bh - pad, bw, bh, mc.options.keyUse.isDown());
        drawKey(g, font, "SPC", ox,            oy - bh - pad, bw, bh, mc.options.keyJump.isDown());
    }

    private void drawKey(GuiGraphics g, Font font, String label, int x, int y, int w, int h, boolean pressed) {
        int bg = pressed ? 0xCCD0D0D0 : 0xAA222222;
        int fg = pressed ? 0xFF000000  : 0xFFCCCCCC;
        g.fill(x, y, x + w, y + h, pressed ? 0xFFD0D0D0 : 0xFF444444);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
        g.drawString(font, label, x + (w - font.width(label)) / 2, y + (h - 9) / 2, fg);
    }
}
