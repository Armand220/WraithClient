package dev.wraith.feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

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

    public void tick(MinecraftClient client) {}
    public void renderHud(MatrixStack stack, TextRenderer tr, int screenW, int screenH) {}
    public void onEnable(MinecraftClient client) {}
    public void onDisable(MinecraftClient client) {}

    public final String   getId()       { return id; }
    public final String   getName()     { return name; }
    public final Category getCategory() { return category; }
    public final boolean  isEnabled()   { return enabled; }

    public final void setEnabled(boolean value) {
        if (enabled == value) return;
        enabled = value;
        MinecraftClient client = MinecraftClient.getInstance();
        if (enabled) onEnable(client);
        else         onDisable(client);
    }

    public final void setEnabledSilent(boolean value) {
        this.enabled = value;
    }
}
