package dev.wraith.feature.impl;

import dev.wraith.WraithModClient;
import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class Keystrokes extends Feature {

    private static final int BW = 18, BH = 14, PAD = 2;

    public Keystrokes() {
        super("keystrokes", "Keystrokes", Category.HUD, true);
        setHudPositionable(340, 310);
    }

    @Override
    public void renderHud(GuiGraphicsExtractor ctx, Font font, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || WraithModClient.isScreenOpen) return;
        int ox = getHudX() + BW + PAD;
        int oy = getHudY() + BH + PAD;
        drawKey(ctx, font, "W",   ox,          oy,          BW, BH, mc.options.keyUp.isDown());
        drawKey(ctx, font, "A",   ox-BW-PAD,   oy+BH+PAD,   BW, BH, mc.options.keyLeft.isDown());
        drawKey(ctx, font, "S",   ox,          oy+BH+PAD,   BW, BH, mc.options.keyDown.isDown());
        drawKey(ctx, font, "D",   ox+BW+PAD,   oy+BH+PAD,   BW, BH, mc.options.keyRight.isDown());
        drawKey(ctx, font, "LMB", ox-BW-PAD,   oy-BH-PAD,   BW, BH, mc.options.keyAttack.isDown());
        drawKey(ctx, font, "RMB", ox+BW+PAD,   oy-BH-PAD,   BW, BH, mc.options.keyUse.isDown());
        drawKey(ctx, font, "SPC", ox,          oy-BH-PAD,   BW, BH, mc.options.keyJump.isDown());
    }

    private void drawKey(GuiGraphicsExtractor ctx, Font font, String label,
                         int x, int y, int w, int h, boolean pressed) {
        int bg     = pressed ? 0xCCD0D0D0 : 0xAA222222;
        int fg     = pressed ? 0xFF000000  : 0xFFCCCCCC;
        int border = pressed ? 0xFFD0D0D0  : 0xFF444444;
        ctx.fill(x, y, x + w, y + h, border);
        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
        ctx.text(font, label, x + (w - font.width(label)) / 2, y + (h - 9) / 2, fg, false);
    }

    @Override public int hudBoxW() { return 3 * BW + 2 * PAD; }
    @Override public int hudBoxH() { return 3 * BH + 2 * PAD; }
}
