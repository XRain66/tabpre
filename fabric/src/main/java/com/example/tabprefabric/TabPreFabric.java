package com.example.tabprefabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;

public class TabPreFabric implements ModInitializer {
    public static final Identifier GAMEMODE_CHANNEL = new Identifier("tabpre", "gamemode");

    @Override
    public void onInitialize() {
        // 注册游戏模式变更监听器
        GameModeChangeCallback.EVENT.register((player, newGameMode) -> {
            // 创建数据包
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeString(player.getName().getString());
            buf.writeInt(newGameMode.getId());

            // 发送到 Velocity
            ServerPlayNetworking.send((ServerPlayerEntity)player, GAMEMODE_CHANNEL, buf);
            
            System.out.println("Sent gamemode change: " + player.getName().getString() + 
                " -> " + newGameMode.getName());
            return true;
        });
    }
} 