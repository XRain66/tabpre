package com.example.tabprefabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TabPreFabric implements ModInitializer {
    public static final String MOD_ID = "tabpre";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Identifier GAMEMODE_CHANNEL = new Identifier(MOD_ID, "gamemode");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing TabPre Fabric...");
        
        // 注册游戏模式变更监听器
        GameModeChangeCallback.EVENT.register((player, newGameMode) -> {
            try {
                // 创建数据包
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeString(player.getName().getString());
                buf.writeInt(newGameMode.getId());

                // 发送到 Velocity
                ServerPlayNetworking.send(player, GAMEMODE_CHANNEL, buf);
                
                LOGGER.info("Sent gamemode change: {} -> {}", 
                    player.getName().getString(), 
                    newGameMode.getName());
                return true;
            } catch (Exception e) {
                LOGGER.error("Failed to send gamemode change packet: {}", e.getMessage());
                return false;
            }
        });
        
        LOGGER.info("TabPre Fabric initialized successfully!");
    }
} 