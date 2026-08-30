package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ArmorStatus extends Feature {
    private static final EquipmentSlot[] SLOTS  = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final String[]        LABELS = {"H", "C", "L", "B"};

    public ArmorStatus() { super("armor", "Armor Status", Category.HUD, true); }

    @Override
    public void renderHud(GuiGraphics g, Font font, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        int y = sh - 50;
        for (int i = 0; i < SLOTS.length; i++) {
            ItemStack stack = mc.player.getItemBySlot(SLOTS[i]);
            if (stack.isEmpty()) continue;
            int maxDur = stack.getMaxDamage();
            if (maxDur == 0) continue;
            int dur = maxDur - stack.getDamageValue();
            float pct = (float) dur / maxDur;
            int color = pct > 0.5f ? 0xFF55FF55 : pct > 0.25f ? 0xFFFFFF55 : 0xFFFF5555;
            g.drawString(font, LABELS[i] + ": " + dur + "/" + maxDur, sw - 80, y + i * 10, color);
        }
    }
}
