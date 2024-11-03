package net.maxrio22.realisticcrops;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = RealisticCrops.MOD_ID, value = Dist.DEDICATED_SERVER)
public class CropGrowthHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RealisticCrops.MOD_ID);
    private static final int GROWTH_INTERVAL_TICKS = 25; // Cada 25 ticks, el cultivo aumenta de edad
    private static BlockPos currentCropPos = null;
    private static int tickCounter = 0; // Contador de ticks para controlar el crecimiento

    @SubscribeEvent
    public static void onCropPlanted(BlockEvent.EntityPlaceEvent event) {
        // Verificamos si estamos en el servidor y si el jugador plantó el cultivo
        if (!(event.getLevel() instanceof ServerLevel world) || !(event.getEntity() instanceof Player)) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getPlacedBlock();
        Block block = state.getBlock();

        // Si el bloque es un cultivo, inicializamos el crecimiento
        if (block instanceof CropBlock) {
            currentCropPos = pos;
            tickCounter = 0; // Reiniciamos el contador de ticks para el nuevo cultivo
            LOGGER.info("onCropPlanted: Semilla plantada en posición {}. Crecimiento programado cada {} ticks.", pos, GROWTH_INTERVAL_TICKS);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        // Verificar que estamos en el servidor y que es el final del tick
        if (!(event.level instanceof ServerLevel world) || event.phase != TickEvent.Phase.END || currentCropPos == null) return;

        tickCounter++; // Incrementamos el contador de ticks

        // Si el contador de ticks ha alcanzado el intervalo de crecimiento
        if (tickCounter >= GROWTH_INTERVAL_TICKS) {
            BlockState state = world.getBlockState(currentCropPos);

            // Verificar que el bloque sigue siendo un cultivo
            if (state.getBlock() instanceof CropBlock cropBlock) {
                IntegerProperty ageProperty = CropBlock.AGE;
                int age = state.getValue(ageProperty);
                int maxAge = cropBlock.getMaxAge();

                // Incrementar la edad si no ha alcanzado la madurez
                if (age < maxAge) {
                    BlockState newState = state.setValue(ageProperty, age + 1);
                    world.setBlock(currentCropPos, newState, 2);
                    LOGGER.info("onWorldTick: Cultivo en posición {} ha crecido a edad {}.", currentCropPos, age + 1);
                } else {
                    LOGGER.info("onWorldTick: Cultivo en posición {} ha alcanzado la edad máxima {} y continuará maduro.", currentCropPos, maxAge);
                    currentCropPos = null; // Detenemos el seguimiento del cultivo al alcanzar la edad máxima
                }
            }

            tickCounter = 0; // Reiniciamos el contador de ticks para el próximo crecimiento
        }
    }
}
