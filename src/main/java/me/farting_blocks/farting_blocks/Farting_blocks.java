package me.farting_blocks.farting_blocks;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class Farting_blocks implements ModInitializer {

    private final Random random = new Random();
    private SoundEvent fartSound;

    @Override
    public void onInitialize() {
        System.out.println("Farting Blocks Mod loaded!");

        // Eigenen Sound registrieren
        fartSound = Registries.SOUND_EVENT.get(Identifier.of("farting_blocks", "fart"));

        // Block abbauen → pupsen
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            if (!world.isClient() && world instanceof ServerWorld serverWorld) {
                playFartEffect(serverWorld, pos);
            }
        });

        // Sprinten überwachen
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.isSprinting() && random.nextInt(10) == 0) { // 10% Chance pro Tick
                    ServerWorld serverWorld = (ServerWorld) player.getEntityWorld();
                    BlockPos pos = player.getBlockPos();
                    playFartEffect(serverWorld, pos);
                }
            }
        });
    }

    private void playFartEffect(ServerWorld world, BlockPos pos) {
        if (fartSound != null) {
            world.playSound(
                    null,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    fartSound,
                    SoundCategory.BLOCKS,
                    1.0f,
                    1.0f
            );
        }

        // Partikel
        for (int i = 0; i < 5; i++) {
            double offsetX = (random.nextDouble() - 0.5);
            double offsetY = 0.5 + random.nextDouble() * 0.5;
            double offsetZ = (random.nextDouble() - 0.5);
            world.spawnParticles(
                    ParticleTypes.CLOUD,
                    pos.getX() + 0.5 + offsetX,
                    pos.getY() + offsetY,
                    pos.getZ() + 0.5 + offsetZ,
                    1, 0, 0.05, 0, 0
            );
        }
    }
}