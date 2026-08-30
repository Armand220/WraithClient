package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class ItemDurability extends Feature {

    public ItemDurability() {
        super("itemdurability", "Item Durability", Category.HUD, false);
        setHudPositionable(340, 380);
    }

    @Override
    public void renderHud(DrawContext ctx, TextRenderer tr, int sw, int sh) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.currentScreen != null) return;

        ItemStack held = mc.player.getMainHandStack();
        if (held.isEmpty() || !held.isDamageable()) return;

        int max     = held.getMaxDamage();
        int current = max - held.getDamage();
        float pct   = (float) current / max;

        int color = pct > 0.5f ? 0xFF55FF55
                  : pct > 0.25f ? 0xFFFFAA00
                  : 0xFFFF5555;

        String text = current + " / " + max;
        ctx.drawTextWithShadow(tr, text, getHudX(), getHudY(), color);
    }

    @Override public int hudBoxW() { return 70; }
    @Override public int hudBoxH() { return 10; }
}
