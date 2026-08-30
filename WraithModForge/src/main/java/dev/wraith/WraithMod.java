package dev.wraith;

import com.mojang.blaze3d.platform.InputConstants;
import dev.wraith.feature.Feature;
import dev.wraith.feature.FeatureManager;
import dev.wraith.gui.WraithMenuScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(value = "wraith", dist = Dist.CLIENT)
public class WraithMod {

    public static FeatureManager FEATURES;
    private static KeyMapping menuKey;

    public WraithMod(IEventBus modBus) {
        FEATURES = new FeatureManager();
        modBus.addListener(this::onClientSetup);
        modBus.addListener(this::registerKeys);
        NeoForge.EVENT_BUS.register(this);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> FEATURES.applyAll());
    }

    private void registerKeys(RegisterKeyMappingsEvent event) {
        menuKey = new KeyMapping(
            "key.wraith.menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "key.categories.misc"
        );
        event.register(menuKey);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (menuKey.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new WraithMenuScreen());
            } else if (mc.screen instanceof WraithMenuScreen) {
                mc.setScreen(null);
            }
        }
        if (mc.player != null) {
            for (Feature f : FEATURES.getAll()) {
                if (f.isEnabled()) f.tick(mc);
            }
        }
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        for (Feature f : FEATURES.getAll()) {
            if (f.isEnabled()) f.renderHud(event.getGuiGraphics(), mc.font, sw, sh);
        }
    }
}
