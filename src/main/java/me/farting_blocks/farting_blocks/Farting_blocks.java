package me.farting_blocks.farting_blocks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Farting_blocks implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("farting_blocks");
    private static final int SPRINT_FART_COOLDOWN = 40;

    private SoundEvent fartSound;
    private final Map<UUID, Integer> sprintCooldowns = new HashMap<>();

    @Override
    public void onInitialize() {
        LOGGER.info("Farting Blocks Mod loaded!");

        Identifier fartId = Identifier.of("farting_blocks", "fart");
        fartSound = Registry.register(
                Registries.SOUND_EVENT,
                fartId,
                SoundEvent.of(fartId)
        );

        // Event: Block abbauen
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            if (world instanceof ServerWorld serverWorld) {
                playFartEffect(serverWorld, pos);
            }
        });

        // Event: Sprinten
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID id = player.getUuid();
                sprintCooldowns.merge(id, -1, Integer::sum);

                if (player.isSprinting()
                        && sprintCooldowns.getOrDefault(id, 0) <= 0
                        && ThreadLocalRandom.current().nextInt(100) == 0) {

                    // Overworld als Fallback, aber wir holen die echte Welt über den Dimensions-Key
                    RegistryKey<World> dimKey = player.getEntityWorld().getRegistryKey();
                    ServerWorld serverWorld = server.getWorld(dimKey);
                    if (serverWorld == null) serverWorld = server.getOverworld();

                    BlockPos pos = player.getBlockPos();
                    playFartEffect(serverWorld, pos);
                    sprintCooldowns.put(id, SPRINT_FART_COOLDOWN);
                }
            }
        });
    }

    private void playFartEffect(ServerWorld world, BlockPos pos) {
        if (fartSound != null) {
            float pitch = 0.8f + ThreadLocalRandom.current().nextFloat() * 0.4f;
            world.playSound(
                    null,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    fartSound,
                    SoundCategory.PLAYERS,
                    1.0f,
                    pitch
            );
        }

        for (int i = 0; i < 5; i++) {
            double offsetX = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.3;
            double offsetY = 0.2 + ThreadLocalRandom.current().nextDouble() * 0.3;
            double offsetZ = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.3;
            world.spawnParticles(
                    ParticleTypes.SMOKE,
                    pos.getX() + 0.5 + offsetX,
                    pos.getY() + offsetY,
                    pos.getZ() + 0.5 + offsetZ,
                    1, 0.0, 0.0, 0.0, 0.02
            );
        }
    }
}