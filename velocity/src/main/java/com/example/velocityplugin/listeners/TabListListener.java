package com.example.velocityplugin.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.TabListUpdateEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.example.velocityplugin.config.TabPreConfig;
import com.example.velocityplugin.TabPrePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TabListListener {
    private final TabPreConfig config;
    private final ProxyServer server;
    private final TabPrePlugin plugin;
    private final Map<String, String> playerGameModes;
    private static final MinecraftChannelIdentifier GAMEMODE_CHANNEL = 
        MinecraftChannelIdentifier.create("tabpre", "gamemode");

    public TabListListener(TabPreConfig config, ProxyServer server, TabPrePlugin plugin) {
        this.config = config;
        this.server = server;
        this.plugin = plugin;
        this.playerGameModes = new ConcurrentHashMap<>();
        
        // 注册插件消息通道
        server.getChannelRegistrar().register(GAMEMODE_CHANNEL);
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        Player player = event.getPlayer();
        updatePlayerDisplay(player);
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        playerGameModes.remove(player.getUsername());
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(GAMEMODE_CHANNEL)) {
            return;
        }

        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
            String playerName = in.readUTF();
            int gameModeId = in.readInt();
            
            // 如果是旁观模式，更新玩家的游戏模式
            if (gameModeId == 3) {
                playerGameModes.put(playerName, "spectator");
            } else {
                playerGameModes.remove(playerName);
            }
            
            // 更新玩家显示
            server.getPlayer(playerName).ifPresent(this::updatePlayerDisplay);
        } catch (Exception e) {
            plugin.getLogger().error("处理游戏模式消息时发生错误: {}", e.getMessage());
        }
    }

    @Subscribe
    public void onTabListUpdate(TabListUpdateEvent event) {
        updatePlayerDisplay(event.getPlayer());
    }

    private void updatePlayerDisplay(Player player) {
        String playerName = player.getUsername();
        
        // 获取玩家的前缀
        String prefix = config.getPrefixes().getOrDefault(playerName, "");
        
        // 创建显示名称，只使用前缀和玩家名
        String displayName = prefix + playerName;

        // 设置玩家的显示名称
        player.setPlayerListName(LegacyComponentSerializer.legacyAmpersand()
            .deserialize(displayName));
    }

    public void shutdown() {
        playerGameModes.clear();
        server.getChannelRegistrar().unregister(GAMEMODE_CHANNEL);
    }
} 