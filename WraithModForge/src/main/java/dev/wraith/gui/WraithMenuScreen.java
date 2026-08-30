package dev.wraith.gui;

import dev.wraith.WraithMod;
import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class WraithMenuScreen extends Screen {

    private static final int CARD_W   = 140;
    private static final int CARD_H   = 36;
    private static final int CARD_PAD = 6;
    private static final int COL_W    = CARD_W + CARD_PAD;

    public WraithMenuScreen() { super(Component.literal("Wraith")); }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0xBB000000);
        g.drawCenteredString(font, "W R A I T H", width / 2, 18, 0xFFD0D0D0);
        g.drawCenteredString(font, "Right Shift to close", width / 2, 30, 0xFF666666);
        g.fill(width / 2 - 100, 40, width / 2 + 100, 41, 0xFF333333);

        List<Feature> features = WraithMod.FEATURES.getAll();
        Category[] cats = Category.values();
        int startX = (width - cats.length * COL_W + CARD_PAD) / 2;

        for (int ci = 0; ci < cats.length; ci++) {
            Category cat = cats[ci];
            int cx = startX + ci * COL_W;
            int cy = 52;

            g.drawCenteredString(font, cat.name(), cx + CARD_W / 2, cy, 0xFF888888);
            cy += 13;

            for (Feature f : features) {
                if (f.getCategory() != cat) continue;
                boolean hovered = mouseX >= cx && mouseX <= cx + CARD_W && mouseY >= cy && mouseY <= cy + CARD_H;
                boolean enabled = f.isEnabled();
                int border = enabled ? 0xFFD0D0D0 : (hovered ? 0xFF555555 : 0xFF333333);
                int bg     = enabled ? 0xFF1A1A1A : (hovered ? 0xFF181818 : 0xFF111111);
                g.fill(cx, cy, cx + CARD_W, cy + CARD_H, border);
                g.fill(cx + 1, cy + 1, cx + CARD_W - 1, cy + CARD_H - 1, bg);
                g.fill(cx + 10, cy + CARD_H / 2 - 2, cx + 14, cy + CARD_H / 2 + 2, enabled ? 0xFF55FF55 : 0xFF444444);
                g.drawString(font, f.getName(), cx + 20, cy + (CARD_H - 9) / 2, 0xFFEEEEEE);
                if (enabled) {
                    String on = "ON";
                    g.drawString(font, on, cx + CARD_W - font.width(on) - 8, cy + (CARD_H - 9) / 2, 0xFF55FF55);
                }
                cy += CARD_H + CARD_PAD;
            }
        }
        super.render(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);
        List<Feature> features = WraithMod.FEATURES.getAll();
        Category[] cats = Category.values();
        int startX = (width - cats.length * COL_W + CARD_PAD) / 2;
        for (int ci = 0; ci < cats.length; ci++) {
            Category cat = cats[ci];
            int cx = startX + ci * COL_W;
            int cy = 65;
            for (Feature f : features) {
                if (f.getCategory() != cat) continue;
                if (mx >= cx && mx <= cx + CARD_W && my >= cy && my <= cy + CARD_H) {
                    f.setEnabled(!f.isEnabled());
                    WraithMod.FEATURES.save();
                    return true;
                }
                cy += CARD_H + CARD_PAD;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 344 || keyCode == 256) { onClose(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
