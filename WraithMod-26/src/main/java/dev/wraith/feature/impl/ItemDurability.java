package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public class ItemDurability extends Feature {

    public ItemDurability() {
        super("itemdurability", "Item Durability", Category.HUD, false);
        setHudPositionable(340, 380);
    }

    @Override
    public void renderHud(GuiGraphicsExtractor ctx, Font font, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || dev.wraith.WraithModClient.isScreenOpen) return;
        ItemStack held = mc.player.getMainHandItem();
        if (held.isEmpty() || !held.isDamageableItem()) return;
        int max     = held.getMaxDamage();
        int current = max - held.getDamageValue();
        float pct   = (float) current / max;
        int color = pct > 0.5f ? 0xFF55FF55 : pct > 0.25f ? 0xFFFFAA00 : 0xFFFF5555;
        ctx.text(font, current + " / " + max, getHudX(), getHudY(), color, true);
    }

    @Override public int hudBoxW() { return 70; }
    @Override public int hudBoxH() { return 10; }
}
