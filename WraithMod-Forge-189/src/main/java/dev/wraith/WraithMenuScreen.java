package dev.wraith;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

public class WraithMenuScreen extends GuiScreen {

    private static final int BTN_CLOSE = 0;

    @Override
    public void initGui() {
        buttonList.clear();
        int startY = 60;
        List<Feature> features = FeatureRegistry.getAll();
        for (int i = 0; i < features.size(); i++) {
            Feature f = features.get(i);
            buttonList.add(new GuiButton(i + 1, width / 2 - 100, startY + i * 24, 200, 20,
                featureLabel(f)));
        }
        buttonList.add(new GuiButton(BTN_CLOSE, width / 2 - 100, height - 36, 200, 20, "Close"));
    }

    @Override
    protected void actionPerformed(GuiButton btn) throws IOException {
        if (btn.id == BTN_CLOSE) {
            mc.displayGuiScreen(null);
            return;
        }
        List<Feature> features = FeatureRegistry.getAll();
        int idx = btn.id - 1;
        if (idx >= 0 && idx < features.size()) {
            Feature f = features.get(idx);
            f.toggle();
            btn.displayString = featureLabel(f);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "WraithClient", width / 2, 20, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }

    private static String featureLabel(Feature f) {
        return f.getName() + ": " + (f.isEnabled() ? "ON" : "OFF");
    }
}
