package dev.wraith.gui;

import dev.wraith.WraithModClient;
import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class WraithMenuScreen extends Screen {

    private static final int CARD_W   = 140;
    private static final int CARD_H   = 36;
    private static final int CARD_PAD = 6;
    private static final int COL_W    = CARD_W + CARD_PAD;

    public WraithMenuScreen() {
        super(Text.literal("Wraith"));
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        fillGradient(matrices, 0, 0, width, height, 0xBB000000, 0xBB000000);

        drawCenteredTextWithShadow(matrices, textRenderer, "W R A I T H", width / 2, 18, 0xFFD0D0D0);
        drawCenteredTextWithShadow(matrices, textRenderer, "Right Shift to close", width / 2, 30, 0xFF666666);

        fill(matrices, width / 2 - 100, 40, width / 2 + 100, 41, 0xFF333333);

        List<Feature> features = WraithModClient.FEATURES.getAll();
        Category[] cats = Category.values();
        int startX = (width - cats.length * COL_W + CARD_PAD) / 2;

        for (int ci = 0; ci < cats.length; ci++) {
            Category cat = cats[ci];
            int cx = startX + ci * COL_W;
            int cy = 52;

            drawCenteredTextWithShadow(matrices, textRenderer, cat.label, cx + CARD_W / 2, cy, 0xFF888888);
            cy += 13;

            for (Feature f : features) {
                if (f.getCategory() != cat) continue;

                boolean hovered = mouseX >= cx && mouseX <= cx + CARD_W
                               && mouseY >= cy && mouseY <= cy + CARD_H;
                boolean enabled = f.isEnabled();

                int border = enabled ? 0xFFD0D0D0 : (hovered ? 0xFF555555 : 0xFF333333);
                int bg     = enabled ? 0xFF1A1A1A : (hovered ? 0xFF181818 : 0xFF111111);
                int dot    = enabled ? 0xFF55FF55 : 0xFF444444;

                fill(matrices, cx,     cy,     cx + CARD_W,     cy + CARD_H,     border);
                fill(matrices, cx + 1, cy + 1, cx + CARD_W - 1, cy + CARD_H - 1, bg);
                fill(matrices, cx + 10, cy + CARD_H / 2 - 2, cx + 14, cy + CARD_H / 2 + 2, dot);

                drawTextWithShadow(matrices, textRenderer, f.getName(), cx + 20, cy + (CARD_H - 9) / 2, 0xFFEEEEEE);

                if (enabled) {
                    String on = "ON";
                    int bx = cx + CARD_W - textRenderer.getWidth(on) - 8;
                    drawTextWithShadow(matrices, textRenderer, on, bx, cy + (CARD_H - 9) / 2, 0xFF55FF55);
                }

                cy += CARD_H + CARD_PAD;
            }
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        List<Feature> features = WraithModClient.FEATURES.getAll();
        Category[] cats = Category.values();
        int startX = (width - cats.length * COL_W + CARD_PAD) / 2;

        for (int ci = 0; ci < cats.length; ci++) {
            Category cat = cats[ci];
            int cx = startX + ci * COL_W;
            int cy = 65;

            for (Feature f : features) {
                if (f.getCategory() != cat) continue;
                if (mouseX >= cx && mouseX <= cx + CARD_W
                 && mouseY >= cy && mouseY <= cy + CARD_H) {
                    f.setEnabled(!f.isEnabled());
                    WraithModClient.FEATURES.save();
                    return true;
                }
                cy += CARD_H + CARD_PAD;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
