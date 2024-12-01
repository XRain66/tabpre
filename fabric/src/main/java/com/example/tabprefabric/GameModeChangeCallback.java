package com.example.tabprefabric;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

public interface GameModeChangeCallback {
    Event<GameModeChangeCallback> EVENT = EventFactory.createArrayBacked(GameModeChangeCallback.class,
        (listeners) -> (player, gameMode) -> {
            for (GameModeChangeCallback listener : listeners) {
                if (!listener.onChange(player, gameMode)) {
                    return false;
                }
            }
            return true;
        });

    /**
     * 当玩家游戏模式改变时调用
     * @param player 玩家
     * @param gameMode 新的游戏模式
     * @return 如果事件应该继续，返回true；如果应该取消，返回false
     */
    boolean onChange(ServerPlayerEntity player, GameMode gameMode);
} 