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

/**
 * The main class for the Farting Blocks mod.
 * This mod adds humorous farting sounds and particle effects to various in-game actions.
 */
public class Farting_blocks implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("farting_blocks");
    private static final int SPRINT_FART_COOLDOWN = 40; // Cooldown in server ticks (20 ticks = 1 second) for sprint farts.

    private SoundEvent fartSound;
    private final Map<UUID, Integer> sprintCooldowns = new HashMap<>();

    /**
     * This method is called when the mod is initialized.
     * It registers the custom fart sound and sets up event listeners for block breaking and sprinting.
     */
    @Override
    public void onInitialize() {
        LOGGER.info("Farting Blocks Mod loaded!");

        // Register the custom fart sound event.
        Identifier fartId = Identifier.of("farting_blocks", "fart");
        fartSound = Registry.register(
                Registries.SOUND_EVENT,
                fartId,
                SoundEvent.of(fartId)
        );

        /**
         * Event listener for when a player breaks a block.
         * If the world is a server world, a fart effect is played at the block's position.
         */
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            if (world instanceof ServerWorld serverWorld) {
                playFartEffect(serverWorld, pos);
            }
        });

        /**
         * Event listener for server ticks, used to handle sprinting fart effects.
         * This checks all online players for sprinting status and applies a fart effect
         * with a cooldown and a random chance.
         */
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID id = player.getUuid();
                sprintCooldowns.merge(id, -1, Integer::sum); // Decrement cooldown for the player.

                // Check if the player is sprinting, cooldown has expired, and a random chance passes.
                if (player.isSprinting()
                        && sprintCooldowns.getOrDefault(id, 0) <= 0
                        && ThreadLocalRandom.current().nextInt(100) == 0) {

                    // Get the player's current world. Use the Overworld as a fallback if the specific dimension is not found.
                    RegistryKey<World> dimKey = player.getEntityWorld().getRegistryKey();
                    ServerWorld serverWorld = server.getWorld(dimKey);
                    if (serverWorld == null) serverWorld = server.getOverworld();

                    BlockPos pos = player.getBlockPos();
                    playFartEffect(serverWorld, pos);
                    sprintCooldowns.put(id, SPRINT_FART_COOLDOWN); // Reset cooldown for the player.
                }
            }
        });
    }

    /**
     * Plays a fart sound and spawns smoke particles at the given position in the world.
     * The sound pitch is randomized for variety.
     *
     * @param world The server world where the effect should be played.
     * @param pos The BlockPos where the fart effect should originate.
     */
    private void playFartEffect(ServerWorld world, BlockPos pos) {
        if (fartSound != null) {
            float pitch = 0.8f + ThreadLocalRandom.current().nextFloat() * 0.4f; // Randomize pitch between 0.8 and 1.2
            world.playSound(
                    null, // No specific player to associate the sound with (plays for all nearby players)
                    pos.getX() + 0.5, // Center the sound on the block
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    fartSound,
                    SoundCategory.PLAYERS, // Sound category for volume control
                    1.0f, // Volume
                    pitch
            );
        }

        // Spawn smoke particles around the block position.
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