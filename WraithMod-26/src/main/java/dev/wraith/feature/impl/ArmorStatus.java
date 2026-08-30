package dev.wraith.feature.impl;

import dev.wraith.feature.Category;
import dev.wraith.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ArmorStatus extends Feature {

    private static final EquipmentSlot[] SLOTS  = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final String[] LABELS = {"H", "C", "L", "B"};

    public ArmorStatus() {
        super("armor", "Armor Status", Category.HUD, true);
        setHudPositionable(680, 120);
    }

    @Override
    public void renderHud(GuiGraphicsExtractor ctx, Font font, int sw, int sh) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        int baseX = getHudX(), baseY = getHudY();
        for (int i = 0; i < SLOTS.length; i++) {
            ItemStack stack = client.player.getItemBySlot(SLOTS[i]);
            if (stack.isEmpty()) continue;
            int maxDur = stack.getMaxDamage();
            if (maxDur == 0) continue;
            int dur = maxDur - stack.getDamageValue();
            float pct = (float) dur / maxDur;
            int color = pct > 0.5f ? 0xFF55FF55 : pct > 0.25f ? 0xFFFFFF55 : 0xFFFF5555;
            ctx.text(font, LABELS[i] + ": " + dur + "/" + maxDur, baseX, baseY + i * 10, color, true);
        }
    }

    @Override public int hudBoxW() { return 80; }
    @Override public int hudBoxH() { return 40; }
}
