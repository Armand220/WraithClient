package dev.wraith;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class ZoomHandler {

    public static final KeyBinding KEY_ZOOM =
        new KeyBinding("key.wraith.zoom", Keyboard.KEY_C, "key.categories.wraith");

    private static final float ZOOM_FOV = 10f;
    private static float savedFov = 70f;
    private static boolean wasZooming = false;

    static {
        ClientRegistry.registerKeyBinding(KEY_ZOOM);
    }

    @SubscribeEvent
    public void onFov(EntityViewRenderEvent.FOVModifier event) {
        if (!FeatureRegistry.ZOOM.isEnabled()) return;
        if (KEY_ZOOM.isKeyDown()) {
            event.setFOV(ZOOM_FOV);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!FeatureRegistry.ZOOM.isEnabled()) return;
        boolean zooming = KEY_ZOOM.isKeyDown();
        if (zooming && !wasZooming) {
            savedFov = Minecraft.getMinecraft().gameSettings.fovSetting;
        } else if (!zooming && wasZooming) {
            Minecraft.getMinecraft().gameSettings.fovSetting = savedFov;
        }
        wasZooming = zooming;
    }
}
