package me.nzqu.tinyauth;

import com.mojang.logging.LogUtils;
import me.nzqu.tinyauth.capabilities.AuthCapability;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod("tinyauth")
public class TinyAuth {

    public static final Logger LOGGER = LogUtils.getLogger();

    public TinyAuth() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(AuthCapability.class);
        MinecraftForge.EVENT_BUS.register(TinyAuthEventHandler.class);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, TinyAuthConfigHandler.Config);
    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("TinyAuth is starting");
    }


}
