package dev.wraith;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class ZoomHandler {

    private static final KeyBinding KEY_ZOOM = new KeyBinding(
        "key.wraith.zoom", Keyboard.KEY_C, "key.categories.wraith");

    private static final float ZOOM_FOV   = 15f;
    private static float       originalFov = -1f;
    private static boolean     wasZooming  = false;

    public ZoomHandler() {
        ClientRegistry.registerKeyBinding(KEY_ZOOM);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!FeatureRegistry.ZOOM.isEnabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        boolean zooming = KEY_ZOOM.isKeyDown();
        if (zooming && !wasZooming) {
            originalFov = mc.gameSettings.fovSetting;
            mc.gameSettings.fovSetting = ZOOM_FOV;
        } else if (!zooming && wasZooming && originalFov >= 0) {
            mc.gameSettings.fovSetting = originalFov;
            originalFov = -1f;
        }
        wasZooming = zooming;
    }
}
