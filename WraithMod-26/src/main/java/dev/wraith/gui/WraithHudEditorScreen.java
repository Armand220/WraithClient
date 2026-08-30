package dev.wraith.gui;

import dev.wraith.WraithModClient;
import dev.wraith.feature.Feature;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class WraithHudEditorScreen extends Screen {

    private static final int SNAP = 2;

    private static final int C_OVERLAY    = 0x50000000;
    private static final int C_GRID       = 0x0CFFFFFF;
    private static final int C_BOX_EN     = 0xA8141414;
    private static final int C_BOX_DIS    = 0x60141414;
    private static final int C_BDR_IDLE   = 0x50FFFFFF;
    private static final int C_BDR_HOV    = 0xA0FFFFFF;
    private static final int C_BDR_DRAG   = 0xFF55FF55;
    private static final int C_BTN_DONE   = 0xFF1A3A1A;
    private static final int C_BTN_DONE_H = 0xFF2A5A2A;
    private static final int C_BTN_RST    = 0xFF1A1A1A;
    private static final int C_BTN_RST_H  = 0xFF2E2E2E;
    private static final int C_TXT_EN     = 0xFFCCCCCC;
    private static final int C_TXT_DIS    = 0xFF555555;
    private static final int C_TXT_DRAG   = 0xFF88FF88;

    private Feature dragging = null;
    private int     dragOffX = 0;
    private int     dragOffY = 0;

    public WraithHudEditorScreen() {
        super(Component.literal("Wraith HUD Editor"));
    }

    @Override public boolean isPauseScreen() { return false; }

    private int btnY() { return height - 26; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
        // Update drag
        if (dragging != null) {
            long handle = minecraft.getWindow().handle();
            if (GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
                int nx = snap(mx - dragOffX);
                int ny = snap(my - dragOffY);
                nx = Math.max(0, Math.min(nx, width - dragging.hudBoxW()));
                ny = Math.max(0, Math.min(ny, height - dragging.hudBoxH()));
                dragging.setHudPos(nx, ny);
            } else {
                dragging = null;
                WraithModClient.FEATURES.saveLayout();
            }
        }

        // Overlay
        ctx.fill(0, 0, width, height, C_OVERLAY);

        // Grid (full screen)
        int gridStep = 20;
        for (int gx = 0; gx < width; gx += gridStep) ctx.fill(gx, 0, gx + 1, height, C_GRID);
        for (int gy = 0; gy < height; gy += gridStep) ctx.fill(0, gy, width, gy + 1, C_GRID);

        // Module boxes
        for (Feature f : WraithModClient.FEATURES.getAll()) {
            if (f.hasHudPosition()) drawModuleBox(ctx, f, mx, my);
        }

        // Floating buttons (bottom-right corner)
        int bY = btnY();
        int doneW = font.width("Done") + 16;
        int doneX = width - doneW - 8;
        boolean dHov = mx >= doneX && mx < doneX + doneW && my >= bY && my < bY + 18;
        ctx.fill(doneX, bY, doneX + doneW, bY + 18, dHov ? C_BTN_DONE_H : C_BTN_DONE);
        drawBorder(ctx, doneX, bY, doneW, 18, 0x80FFFFFF);
        ctx.text(font, "Done", doneX + (doneW - font.width("Done")) / 2, bY + (18 - 9) / 2, 0xFF88FF88, false);

        int resetW = font.width("Reset") + 16;
        int resetX = doneX - resetW - 6;
        boolean rHov = mx >= resetX && mx < resetX + resetW && my >= bY && my < bY + 18;
        ctx.fill(resetX, bY, resetX + resetW, bY + 18, rHov ? C_BTN_RST_H : C_BTN_RST);
        drawBorder(ctx, resetX, bY, resetW, 18, 0x50FFFFFF);
        ctx.text(font, "Reset", resetX + (resetW - font.width("Reset")) / 2, bY + (18 - 9) / 2, 0xFF888888, false);

        super.extractRenderState(ctx, mx, my, delta);
    }

    private void drawModuleBox(GuiGraphicsExtractor ctx, Feature f, int mx, int my) {
        int x = f.getHudX(), y = f.getHudY(), bw = f.hudBoxW(), bh = f.hudBoxH();
        boolean draggingThis = f == dragging;
        boolean hov = !draggingThis && mx >= x && mx < x + bw && my >= y && my < y + bh;
        boolean en  = f.isEnabled();

        int bg  = draggingThis ? 0xC0162816 : en ? C_BOX_EN : C_BOX_DIS;
        ctx.fill(x, y, x + bw, y + bh, bg);

        int bdr = draggingThis ? C_BDR_DRAG : hov ? C_BDR_HOV : C_BDR_IDLE;
        drawBorder(ctx, x, y, bw, bh, bdr);

        String label = f.getName();
        int maxLabelW = bw - 6;
        while (label.length() > 2 && font.width(label) > maxLabelW)
            label = label.substring(0, label.length() - 1);
        if (!label.equals(f.getName())) label = label.substring(0, label.length() - 1) + "..";

        int tc = draggingThis ? C_TXT_DRAG : en ? C_TXT_EN : C_TXT_DIS;
        ctx.text(font, label, x + 3, y + (bh - 9) / 2, tc, false);

        if (hov || draggingThis) {
            ctx.fill(x + bw - 5, y + 2, x + bw - 4, y + 3, bdr);
            ctx.fill(x + bw - 5, y + 4, x + bw - 4, y + 5, bdr);
            ctx.fill(x + bw - 5, y + 6, x + bw - 4, y + 7, bdr);
        }
    }

    private static void drawBorder(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int c) {
        ctx.fill(x,         y,         x + w, y + 1,     c);
        ctx.fill(x,         y + h - 1, x + w, y + h,     c);
        ctx.fill(x,         y,         x + 1, y + h,     c);
        ctx.fill(x + w - 1, y,         x + w, y + h,     c);
    }

    private static int snap(int v) { return (v / SNAP) * SNAP; }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean handled) {
        if (event.button() != 0) return super.mouseClicked(event, handled);
        int mx = (int) event.x(), my = (int) event.y();
        int bY = btnY();

        int doneW = font.width("Done") + 16;
        int doneX = width - doneW - 8;
        if (mx >= doneX && mx < doneX + doneW && my >= bY && my < bY + 18) {
            WraithModClient.FEATURES.saveLayout();
            this.minecraft.setScreenAndShow(null);
            return true;
        }

        int resetW = font.width("Reset") + 16;
        int resetX = doneX - resetW - 6;
        if (mx >= resetX && mx < resetX + resetW && my >= bY && my < bY + 18) {
            WraithModClient.FEATURES.getAll().forEach(f -> { if (f.hasHudPosition()) f.resetHudPos(); });
            WraithModClient.FEATURES.saveLayout();
            return true;
        }

        List<Feature> all = WraithModClient.FEATURES.getAll();
        for (int i = all.size() - 1; i >= 0; i--) {
            Feature f = all.get(i);
            if (!f.hasHudPosition()) continue;
            int fx = f.getHudX(), fy = f.getHudY();
            if (mx >= fx && mx < fx + f.hudBoxW() && my >= fy && my < fy + f.hudBoxH()) {
                dragging = f;
                dragOffX = mx - fx;
                dragOffY = my - fy;
                return true;
            }
        }
        return super.mouseClicked(event, handled);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            WraithModClient.FEATURES.saveLayout();
            this.minecraft.setScreenAndShow(null);
            return true;
        }
        return super.keyPressed(event);
    }
}
