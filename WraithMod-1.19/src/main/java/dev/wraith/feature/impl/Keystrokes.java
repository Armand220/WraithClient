package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import dev.wraith.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class Keystrokes extends Feature {

    public Keystrokes() {
        super("keystrokes", "Keystrokes", Category.HUD, true);
    }

    @Override
    public void renderHud(MatrixStack stack, TextRenderer tr, int sw, int sh) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.currentScreen != null) return;

        int bw = 18, bh = 14, pad = 2;
        int ox = sw / 2 - bw - pad / 2;
        int oy = sh - 80;

        drawKey(stack, tr, "W",   ox,            oy,            bw, bh, mc.options.forwardKey.isPressed());
        drawKey(stack, tr, "A",   ox - bw - pad, oy + bh + pad, bw, bh, mc.options.leftKey.isPressed());
        drawKey(stack, tr, "S",   ox,            oy + bh + pad, bw, bh, mc.options.backKey.isPressed());
        drawKey(stack, tr, "D",   ox + bw + pad, oy + bh + pad, bw, bh, mc.options.rightKey.isPressed());
        drawKey(stack, tr, "LMB", ox - bw - pad, oy - bh - pad, bw, bh, mc.options.attackKey.isPressed());
        drawKey(stack, tr, "RMB", ox + bw + pad, oy - bh - pad, bw, bh, mc.options.useKey.isPressed());
        drawKey(stack, tr, "SPC", ox,            oy - bh - pad, bw, bh, mc.options.jumpKey.isPressed());
    }

    private void drawKey(MatrixStack stack, TextRenderer tr, String label, int x, int y, int w, int h, boolean pressed) {
        int bg     = pressed ? 0xCCD0D0D0 : 0xAA222222;
        int fg     = pressed ? 0xFF000000  : 0xFFCCCCCC;
        int border = pressed ? 0xFFD0D0D0 : 0xFF444444;

        RenderUtil.fill(stack, x, y, x + w, y + h, border);
        RenderUtil.fill(stack, x + 1, y + 1, x + w - 1, y + h - 1, bg);

        int tx = x + (w - tr.getWidth(label)) / 2;
        int ty = y + (h - 9) / 2;
        tr.draw(stack, label, (float) tx, (float) ty, fg);
    }
}
