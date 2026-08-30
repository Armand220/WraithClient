package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class Keystrokes extends Feature {

    // key cell dimensions
    private static final int BW = 18, BH = 14, PAD = 2;
    // total bounding box: 3*BW + 2*PAD × 3*BH + 2*PAD = 58 × 46
    // stored position is the top-left corner of that bounding box

    public Keystrokes() {
        super("keystrokes", "Keystrokes", Category.HUD, true);
        setHudPositionable(340, 310);
    }

    @Override
    public void renderHud(DrawContext ctx, TextRenderer tr, int sw, int sh) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.currentScreen != null) return;

        // ox / oy are the top-left of the "W" key (middle of the top row)
        // bounding box starts at (ox - BW - PAD, oy - BH - PAD)
        int ox = getHudX() + BW + PAD;
        int oy = getHudY() + BH + PAD;

        drawKey(ctx, tr, "W",   ox,          oy,          BW, BH, mc.options.forwardKey.isPressed());
        drawKey(ctx, tr, "A",   ox-BW-PAD,   oy+BH+PAD,   BW, BH, mc.options.leftKey.isPressed());
        drawKey(ctx, tr, "S",   ox,          oy+BH+PAD,   BW, BH, mc.options.backKey.isPressed());
        drawKey(ctx, tr, "D",   ox+BW+PAD,   oy+BH+PAD,   BW, BH, mc.options.rightKey.isPressed());
        drawKey(ctx, tr, "LMB", ox-BW-PAD,   oy-BH-PAD,   BW, BH, mc.options.attackKey.isPressed());
        drawKey(ctx, tr, "RMB", ox+BW+PAD,   oy-BH-PAD,   BW, BH, mc.options.useKey.isPressed());
        drawKey(ctx, tr, "SPC", ox,          oy-BH-PAD,   BW, BH, mc.options.jumpKey.isPressed());
    }

    private void drawKey(DrawContext ctx, TextRenderer tr, String label,
                         int x, int y, int w, int h, boolean pressed) {
        int bg     = pressed ? 0xCCD0D0D0 : 0xAA222222;
        int fg     = pressed ? 0xFF000000  : 0xFFCCCCCC;
        int border = pressed ? 0xFFD0D0D0  : 0xFF444444;

        ctx.fill(x, y, x + w, y + h, border);
        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);

        int tx = x + (w - tr.getWidth(label)) / 2;
        int ty = y + (h - 9) / 2;
        ctx.drawText(tr, label, tx, ty, fg, false);
    }

    @Override public int hudBoxW() { return 3 * BW + 2 * PAD; }
    @Override public int hudBoxH() { return 3 * BH + 2 * PAD; }
}
