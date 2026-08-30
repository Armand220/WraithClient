package dev.wraith;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = WraithMod.MODID, name = "WraithClient", version = "1.0.0",
     clientSideOnly = true, acceptedMinecraftVersions = "[1.12,1.13)")
public class WraithMod {
    public static final String MODID = "wraith";

    @Mod.Instance
    public static WraithMod instance;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        FeatureRegistry.init();
        MinecraftForge.EVENT_BUS.register(new HudRenderer());
        MinecraftForge.EVENT_BUS.register(new KeyHandler());
        MinecraftForge.EVENT_BUS.register(new ZoomHandler());
    }
}
