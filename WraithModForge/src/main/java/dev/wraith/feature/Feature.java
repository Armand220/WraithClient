package dev.wraith.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public abstract class Feature {

    private final String   id;
    private final String   name;
    private final Category category;
    private boolean enabled;

    protected Feature(String id, String name, Category category, boolean defaultEnabled) {
        this.id       = id;
        this.name     = name;
        this.category = category;
        this.enabled  = defaultEnabled;
    }

    public void tick(Minecraft mc) {}
    public void renderHud(GuiGraphics g, Font font, int sw, int sh) {}
    public void onEnable(Minecraft mc) {}
    public void onDisable(Minecraft mc) {}

    public final String   getId()       { return id; }
    public final String   getName()     { return name; }
    public final Category getCategory() { return category; }
    public final boolean  isEnabled()   { return enabled; }

    public final void setEnabled(boolean value) {
        if (enabled == value) return;
        enabled = value;
        Minecraft mc = Minecraft.getInstance();
        if (enabled) onEnable(mc);
        else         onDisable(mc);
    }

    public final void setEnabledSilent(boolean value) {
        this.enabled = value;
    }
}
