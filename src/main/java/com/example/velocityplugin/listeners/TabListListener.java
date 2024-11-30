package com.example.velocityplugin.listeners;

import com.example.velocityplugin.config.TabPreConfig;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class TabListListener {
    private final TabPreConfig config;
    private final ProxyServer server;

    public TabListListener(TabPreConfig config, ProxyServer server) {
        this.config = config;
        this.server = server;
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        updatePlayerPrefix(event.getPlayer());
        // 更新所有玩家的Tab列表
        updateAllPlayers();
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        // 当玩家切换服务器时更新
        updatePlayerPrefix(event.getPlayer());
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        // 玩家断开连接时更新其他玩家的Tab列表
        server.getAllPlayers().forEach(this::updatePlayerPrefix);
    }

    private void updatePlayerPrefix(Player player) {
        String playerName = player.getUsername();
        if (config.hasPrefix(playerName)) {
            String prefix = config.getPrefix(playerName);
            player.setPlayerListName(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(prefix + playerName));
        } else {
            // 如果没有前缀配置，恢复默认名称
            player.setPlayerListName(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(playerName));
        }
    }

    private void updateAllPlayers() {
        server.getAllPlayers().forEach(this::updatePlayerPrefix);
    }

    // 提供一个公共方法用于手动更新所有玩家（例如配置重载后）
    public void refreshAllPlayers() {
        updateAllPlayers();
    }
} 