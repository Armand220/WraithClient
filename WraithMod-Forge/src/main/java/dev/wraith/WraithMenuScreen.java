package dev.wraith;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;

public class WraithMenuScreen extends GuiScreen {

    private static final int CARD_W = 110;
    private static final int CARD_H = 40;
    private static final int COLS   = 3;
    private static final int PAD    = 10;

    @Override
    public void initGui() {
        List<Feature> features = FeatureRegistry.getAll();
        int startX = width / 2 - (COLS * (CARD_W + PAD) - PAD) / 2;
        int startY = 60;
        for (int i = 0; i < features.size(); i++) {
            int col = i % COLS;
            int row = i / COLS;
            int x = startX + col * (CARD_W + PAD);
            int y = startY + row * (CARD_H + PAD);
            buttonList.add(new FeatureButton(i, x, y, features.get(i)));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0xBB000000);
        drawCenteredString(fontRenderer, "W R A I T H", width / 2, 18, 0xFFD0D0D0);
        drawCenteredString(fontRenderer, "Right Shift to close", width / 2, 30, 0xFF666666);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button instanceof FeatureButton) {
            ((FeatureButton) button).feature.toggle();
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_RSHIFT) {
            mc.displayGuiScreen(null);
        }
    }

    private static class FeatureButton extends GuiButton {
        final Feature feature;

        FeatureButton(int id, int x, int y, Feature feature) {
            super(id, x, y, CARD_W, CARD_H, feature.getName());
            this.feature = feature;
        }

        @Override
        public void drawButton(net.minecraft.client.Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (!visible) return;
            boolean on = feature.isEnabled();
            int bg = on ? 0xCC1A2A1A : 0xCC111111;
            int border = on ? 0xFF5CB85C : 0xFF222222;
            int textCol = on ? 0xFF5CB85C : 0xFFAAAAAA;

            drawRect(x, y, x + width, y + height, bg);
            // border lines
            drawRect(x, y,          x + width,     y + 1,          border);
            drawRect(x, y + height - 1, x + width, y + height,     border);
            drawRect(x, y,          x + 1,         y + height,     border);
            drawRect(x + width - 1, y, x + width,  y + height,     border);

            mc.fontRenderer.drawStringWithShadow(displayString, x + width / 2f - mc.fontRenderer.getStringWidth(displayString) / 2f, y + height / 2f - 4, textCol);
            String tag = on ? "ON" : "OFF";
            mc.fontRenderer.drawStringWithShadow(tag, x + width / 2f - mc.fontRenderer.getStringWidth(tag) / 2f, y + height / 2f + 5, on ? 0xFF5CB85C : 0xFF555555);
        }
    }
}
