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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
        // 延迟一下更新，等待后端服务器的 TabList 先发送完
        server.getScheduler()
            .buildTask(this, () -> server.getAllPlayers().forEach(this::updateTabListForPlayer))
            .delay(100, TimeUnit.MILLISECONDS)
            .schedule();
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        // 延迟一下更新，等待后端服务器的 TabList 先发送完
        server.getScheduler()
            .buildTask(this, () -> server.getAllPlayers().forEach(this::updateTabListForPlayer))
            .delay(100, TimeUnit.MILLISECONDS)
            .schedule();
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
        // 先移除所有现有条目
        Set<UUID> existingEntries = new HashSet<>();
        viewer.getTabList().getEntries().forEach(entry -> existingEntries.add(entry.getProfile().getId()));
        existingEntries.forEach(uuid -> viewer.getTabList().removeEntry(uuid));
        
        // 重新添加所有玩家
        for (Player target : server.getAllPlayers()) {
            Component displayName = getDisplayName(target);
            viewer.getTabList().addEntry(TabListEntry.builder()
                .profile(target.getGameProfile())
                .displayName(displayName)
                .tabList(viewer.getTabList())
                .latency((int) target.getPing())
                .gameMode(0)
                .build());
        }
    }

    private Component getDisplayName(Player player) {
        String playerName = player.getUsername();
        if (config.hasPrefix(playerName)) {
            String prefix = config.getPrefix(playerName);
            return LegacyComponentSerializer.legacyAmpersand()
                .deserialize(prefix + " " + playerName);
        }
        return Component.text(playerName);
    }

    // 提供一个公共方法用于手动更新所有玩家的Tab列表（例如配置重载后）
    public void refreshAllPlayers() {
        server.getAllPlayers().forEach(this::updateTabListForPlayer);
    }
} 