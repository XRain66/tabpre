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
import java.util.HashMap;
import java.util.Map;

public class TabListListener {
    private final TabPreConfig config;
    private final ProxyServer server;

    public TabListListener(TabPreConfig config, ProxyServer server) {
        this.config = config;
        this.server = server;
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        Player player = event.getPlayer();
        // 延迟 500ms 后更新所有玩家的 TabList
        server.getScheduler()
            .buildTask(this, () -> {
                Map<UUID, TabListEntry> originalEntries = new HashMap<>();
                player.getTabList().getEntries().forEach(entry -> 
                    originalEntries.put(entry.getProfile().getId(), entry));
                
                server.getAllPlayers().forEach(viewer -> 
                    updateTabListForPlayer(viewer, originalEntries));
            })
            .delay(500, TimeUnit.MILLISECONDS)
            .schedule();
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        // 延迟 500ms 后更新所有玩家的 TabList
        server.getScheduler()
            .buildTask(this, () -> {
                Map<UUID, TabListEntry> originalEntries = new HashMap<>();
                player.getTabList().getEntries().forEach(entry -> 
                    originalEntries.put(entry.getProfile().getId(), entry));
                
                server.getAllPlayers().forEach(viewer -> 
                    updateTabListForPlayer(viewer, originalEntries));
            })
            .delay(500, TimeUnit.MILLISECONDS)
            .schedule();
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        // 获取玩家的游戏模式信息
        Player player = event.getPlayer();
        
        // 延迟 500ms 后更新所有玩家的 TabList，确保已经收到了后端服务器的 TabList 数据
        server.getScheduler()
            .buildTask(this, () -> {
                // 获取当前玩家的 TabList 信息（这时已经包含了后端服务器发送的信息）
                Map<UUID, TabListEntry> originalEntries = new HashMap<>();
                player.getTabList().getEntries().forEach(entry -> 
                    originalEntries.put(entry.getProfile().getId(), entry));
                
                // 为所有在线玩家更新 TabList，但保持原有的游戏模式和其他信息
                server.getAllPlayers().forEach(viewer -> 
                    updateTabListForPlayer(viewer, originalEntries));
            })
            .delay(500, TimeUnit.MILLISECONDS)
            .schedule();
    }

    private void updateTabListForPlayer(Player viewer, Map<UUID, TabListEntry> originalEntries) {
        // 清空现有条目
        Set<UUID> existingEntries = new HashSet<>();
        viewer.getTabList().getEntries().forEach(entry -> 
            existingEntries.add(entry.getProfile().getId()));
        existingEntries.forEach(uuid -> viewer.getTabList().removeEntry(uuid));
        
        // 重新添加所有玩家，使用原始 TabList 中的信息
        for (Player target : server.getAllPlayers()) {
            Component displayName = getDisplayName(target);
            
            // 从原始 TabList 中获取游戏模式等信息，如果没有则使用默认值
            TabListEntry originalEntry = originalEntries.get(target.getUniqueId());
            int gameMode = originalEntry != null ? originalEntry.getGameMode() : 0;
            
            viewer.getTabList().addEntry(TabListEntry.builder()
                .profile(target.getGameProfile())
                .displayName(displayName)
                .tabList(viewer.getTabList())
                .latency((int) target.getPing())
                .gameMode(gameMode)  // 使用原始 TabList 中的游戏模式
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
        // 获取任意一个玩家的 TabList 信息作为原始信息
        Player anyPlayer = server.getAllPlayers().stream().findFirst().orElse(null);
        if (anyPlayer != null) {
            Map<UUID, TabListEntry> originalEntries = new HashMap<>();
            anyPlayer.getTabList().getEntries().forEach(entry -> 
                originalEntries.put(entry.getProfile().getId(), entry));
            
            // 为所有在线玩家更新 TabList
            server.getAllPlayers().forEach(viewer -> 
                updateTabListForPlayer(viewer, originalEntries));
        }
    }
} 