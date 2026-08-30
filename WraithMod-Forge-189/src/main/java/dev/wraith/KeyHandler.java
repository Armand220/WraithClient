package dev.wraith;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeyHandler {

    public static final KeyBinding KEY_MENU = new KeyBinding(
        "key.wraith.menu", Keyboard.KEY_RSHIFT, "key.categories.wraith");

    public KeyHandler() {
        ClientRegistry.registerKeyBinding(KEY_MENU);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        if (KEY_MENU.isPressed()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new WraithMenuScreen());
            }
        }
    }
}
