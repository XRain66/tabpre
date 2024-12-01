package com.example.tabprefabric;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

public interface GameModeChangeCallback {
    Event<GameModeChangeCallback> EVENT = EventFactory.createArrayBacked(GameModeChangeCallback.class,
            (listeners) -> (player, newGameMode) -> {
                for (GameModeChangeCallback listener : listeners) {
                    listener.onChange(player, newGameMode);
                }
            });

    boolean onChange(ServerPlayerEntity player, GameMode newGameMode);
}