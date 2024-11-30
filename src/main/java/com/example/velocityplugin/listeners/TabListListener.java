package com.example.velocityplugin.listeners;

import com.example.velocityplugin.config.TabPreConfig;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.TabListEntry;
import net.kyori.adventure.text.Component;
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
        Player joiningPlayer = event.getPlayer();
        // 为新加入的玩家更新所有玩家的显示名称
        updateTabListForPlayer(joiningPlayer);
        // 为所有在线玩家更新新玩家的显示名称
        updatePlayerForAll(joiningPlayer);
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        // 当玩家切换服务器时更新
        Player player = event.getPlayer();
        updateTabListForPlayer(player);
        updatePlayerForAll(player);
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        Player disconnectedPlayer = event.getPlayer();
        // 只更新其他在线玩家的 Tab 列表
        server.getAllPlayers().stream()
            .filter(p -> !p.equals(disconnectedPlayer))
            .forEach(this::updateTabListForPlayer);
    }

    private void updateTabListForPlayer(Player viewer) {
        for (Player target : server.getAllPlayers()) {
            // 如果目标玩家就是查看者，可以跳过
            if (target.equals(viewer)) {
                continue;
            }
            
            Component displayName = getDisplayName(target);
            viewer.getTabList().removeEntry(target.getUniqueId());
            viewer.getTabList().addEntry(
                TabListEntry.builder()
                    .profile(target.getGameProfile())
                    .displayName(displayName)
                    .tabList(viewer.getTabList())
                    .build()
            );
        }
    }

    private void updatePlayerForAll(Player target) {
        Component displayName = getDisplayName(target);
        
        for (Player viewer : server.getAllPlayers()) {
            // 跳过目标玩家自己
            if (target.equals(viewer)) {
                continue;
            }
            
            viewer.getTabList().removeEntry(target.getUniqueId());
            TabListEntry entry = TabListEntry.builder()
                .profile(target.getGameProfile())
                .displayName(displayName)
                .tabList(viewer.getTabList())
                .build();
            viewer.getTabList().addEntry(entry);
        }
    }

    private Component getDisplayName(Player player) {
        String playerName = player.getUsername();
        if (config.hasPrefix(playerName)) {
            String prefix = config.getPrefix(playerName);
            return LegacyComponentSerializer.legacyAmpersand()
                .deserialize(prefix + playerName);
        }
        return Component.text(playerName);
    }

    // 提供一个公共方法用于手动更新所有玩家的Tab列表（例如配置重载后）
    public void refreshAllPlayers() {
        for (Player viewer : server.getAllPlayers()) {
            updateTabListForPlayer(viewer);
            updatePlayerForAll(viewer);
        }
    }
} 