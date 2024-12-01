package com.example.velocityplugin.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.example.velocityplugin.config.TabPreConfig;
import com.example.velocityplugin.TabPrePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TabListListener {
    private final TabPreConfig config;
    private final ProxyServer server;
    private final TabPrePlugin plugin;
    private final Set<String> spectatorPlayers;
    private static final MinecraftChannelIdentifier GAMEMODE_CHANNEL = 
        MinecraftChannelIdentifier.create("tabpre", "gamemode");

    public TabListListener(TabPreConfig config, ProxyServer server, TabPrePlugin plugin) {
        this.config = config;
        this.server = server;
        this.plugin = plugin;
        this.spectatorPlayers = ConcurrentHashMap.newKeySet();
        
        // 注册插件消息通道
        server.getChannelRegistrar().register(GAMEMODE_CHANNEL);
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        Player player = event.getPlayer();
        updatePlayerDisplay(player);
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        updatePlayerDisplay(player);
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        spectatorPlayers.remove(player.getUsername());
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
            
            // 如果是旁观模式，添加到旁观玩家集合，否则移除
            if (gameModeId == 3) {
                spectatorPlayers.add(playerName);
            } else {
                spectatorPlayers.remove(playerName);
            }
            
            // 更新玩家显示
            server.getPlayer(playerName).ifPresent(this::updatePlayerDisplay);
        } catch (Exception e) {
            plugin.getLogger().error("处理游戏模式消息时发生错误: {}", e.getMessage());
        }
    }

    private void updatePlayerDisplay(Player player) {
        String playerName = player.getUsername();
        
        // 获取玩家的前缀
        String prefix = config.getPrefix(playerName);
        
        // 创建显示名称，只使用前缀和玩家名
        String displayName = prefix + playerName;

        // 使用 LegacyComponentSerializer 处理颜色代码
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(displayName);
        player.setPlayerListName(component);
    }

    public void shutdown() {
        spectatorPlayers.clear();
        server.getChannelRegistrar().unregister(GAMEMODE_CHANNEL);
    }

    public void refreshAllPlayers() {
        server.getAllPlayers().forEach(this::updatePlayerDisplay);
    }
} 