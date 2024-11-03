package net.maxrio22.realisticcrops;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(RealisticCrops.MOD_ID)
public class RealisticCrops {
    public static final String MOD_ID = "realisticcrops";
    private static final Logger LOGGER = LogUtils.getLogger();

    public RealisticCrops(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Registro de configuración común
        modEventBus.addListener(this::commonSetup);

        // Registro de eventos en el bus de Forge, adecuado para eventos de nivel de juego
        MinecraftForge.EVENT_BUS.register(new CropGrowthHandler());

        LOGGER.info("RealisticCrops: Mod inicializado en el servidor.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("RealisticCrops: Configuración común completada.");
    }
}
