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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
@Mod.EventBusSubscriber(modid = RealisticCrops.MOD_ID, value = Dist.DEDICATED_SERVER)
public class CropGrowthHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RealisticCrops.MOD_ID);
    private static final long TOTAL_GROWTH_MILLISECONDS = Utils.timeToMilliseconds("10s"); // Tiempo total de crecimiento en milisegundos
    private static final Map<BlockPos, Long> cropStartTimes = new HashMap<>(); // Guarda el tiempo de inicio de cada cultivo
    private static final Map<BlockPos, Long> growthIntervals = new HashMap<>(); // Intervalo en milisegundos para cada cultivo

    @SubscribeEvent
    public static void onCropPlanted(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel world) || !(event.getEntity() instanceof Player)) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getPlacedBlock();
        Block block = state.getBlock();

        if (block instanceof CropBlock cropBlock) {
            int maxAge = cropBlock.getMaxAge();
            long growthIntervalMillis = TOTAL_GROWTH_MILLISECONDS / maxAge; // Intervalo en milisegundos

            cropStartTimes.put(pos, System.currentTimeMillis()); // Tiempo de inicio del cultivo
            growthIntervals.put(pos, growthIntervalMillis); // Guardamos el intervalo de crecimiento

            LOGGER.info("onCropPlanted: Semilla plantada en posición {}. Crecerá cada {} ms.", pos, growthIntervalMillis);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (!(event.level instanceof ServerLevel world) || event.phase != TickEvent.Phase.END) return;

        long currentTime = System.currentTimeMillis(); // Tiempo actual

        Iterator<Map.Entry<BlockPos, Long>> iterator = cropStartTimes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Long> entry = iterator.next();
            BlockPos pos = entry.getKey();
            long startTime = entry.getValue();
            long growthIntervalMillis = growthIntervals.get(pos);

            // Tiempo transcurrido desde que se plantó el cultivo
            long elapsedTime = currentTime - startTime;
            BlockState state = world.getBlockState(pos);

            if (state.getBlock() instanceof CropBlock cropBlock) {
                IntegerProperty ageProperty = CropBlock.AGE;

                // Verifica si el bloque tiene la propiedad 'age'
                if (!state.hasProperty(ageProperty)) {
                    LOGGER.warn("El bloque en posición {} no tiene la propiedad 'age'. Ignorando crecimiento.", pos);
                    continue;
                }

                int age = state.getValue(ageProperty);
                int maxAge = cropBlock.getMaxAge();

                // Calcula la edad esperada del cultivo en función del tiempo transcurrido
                int expectedAge = Math.min((int) (elapsedTime / growthIntervalMillis), maxAge);

                if (expectedAge > age) {
                    // Actualiza la edad del cultivo solo si ha alcanzado la siguiente etapa
                    BlockState newState = state.setValue(ageProperty, expectedAge);
                    world.setBlock(pos, newState, 2);
                    LOGGER.info("onLevelTick: Cultivo en posición {} ha crecido a edad {}.", pos, expectedAge);
                }

                // Detener el crecimiento si ha alcanzado la edad máxima
                if (expectedAge >= maxAge) {
                    iterator.remove();
                    growthIntervals.remove(pos);
                    LOGGER.info("onLevelTick: Cultivo en posición {} ha alcanzado la edad máxima {} y se detendrá el crecimiento.", pos, maxAge);
                }
            }
        }
    }
}

