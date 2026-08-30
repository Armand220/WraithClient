package dev.wraith;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeyHandler {

    public static final KeyBinding KEY_MENU =
        new KeyBinding("key.wraith.menu", Keyboard.KEY_RSHIFT, "key.categories.wraith");

    public static final KeyBinding KEY_FULLBRIGHT =
        new KeyBinding("key.wraith.fullbright", Keyboard.KEY_NONE, "key.categories.wraith");

    static {
        ClientRegistry.registerKeyBinding(KEY_MENU);
        ClientRegistry.registerKeyBinding(KEY_FULLBRIGHT);
    }

    private static final Minecraft MC = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        if (KEY_MENU.isPressed()) {
            if (MC.currentScreen == null)
                MC.displayGuiScreen(new WraithMenuScreen());
            else if (MC.currentScreen instanceof WraithMenuScreen)
                MC.displayGuiScreen(null);
        }

        if (KEY_FULLBRIGHT.isPressed()) {
            FeatureRegistry.FULLBRIGHT.toggle();
            float gamma = FeatureRegistry.FULLBRIGHT.isEnabled() ? 15.0f : MC.gameSettings.gammaSetting;
            MC.gameSettings.gammaSetting = gamma;
        }
    }
}
