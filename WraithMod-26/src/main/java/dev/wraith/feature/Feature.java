package dev.wraith.feature;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class Feature {

    private final String   id;
    private final String   name;
    private final Category category;
    private boolean enabled;

    // HUD layout editor position
    private int     hudX            = 0;
    private int     hudY            = 0;
    private int     defaultHudX     = 0;
    private int     defaultHudY     = 0;
    private boolean hudPositionable = false;

    protected Feature(String id, String name, Category category, boolean defaultEnabled) {
        this.id       = id;
        this.name     = name;
        this.category = category;
        this.enabled  = defaultEnabled;
    }

    protected void setHudPositionable(int defaultX, int defaultY) {
        this.hudPositionable = true;
        this.hudX            = defaultX;
        this.hudY            = defaultY;
        this.defaultHudX     = defaultX;
        this.defaultHudY     = defaultY;
    }

    public boolean hasHudPosition() { return hudPositionable; }
    public int     getHudX()        { return hudX; }
    public int     getHudY()        { return hudY; }
    public void    setHudPos(int x, int y) { this.hudX = x; this.hudY = y; }
    public void    resetHudPos()    { this.hudX = defaultHudX; this.hudY = defaultHudY; }
    public int     hudBoxW()        { return 80; }
    public int     hudBoxH()        { return 10; }

    public void tick(Minecraft client) {}
    public void renderHud(GuiGraphicsExtractor ctx, Font font, int screenW, int screenH) {}
    public void onEnable(Minecraft client) {}
    public void onDisable(Minecraft client) {}

    public final String   getId()       { return id; }
    public final String   getName()     { return name; }
    public final Category getCategory() { return category; }
    public final boolean  isEnabled()   { return enabled; }

    public final void setEnabled(boolean value) {
        if (enabled == value) return;
        enabled = value;
        Minecraft client = Minecraft.getInstance();
        if (enabled) onEnable(client);
        else         onDisable(client);
    }

    public final void setEnabledSilent(boolean value) { this.enabled = value; }
}
