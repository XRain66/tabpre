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
    private final Object plugin;

    public TabListListener(TabPreConfig config, ProxyServer server, Object plugin) {
        this.config = config;
        this.server = server;
        this.plugin = plugin;
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        Player player = event.getPlayer();
        // 延迟获取游戏模式信息
        server.getScheduler()
            .buildTask(plugin, () -> {
                Map<UUID, TabListEntry> originalEntries = new HashMap<>();
                
                // 从所有玩家的 TabList 中收集信息
                server.getAllPlayers().forEach(p -> {
                    p.getTabList().getEntries().stream()
                        .filter(entry -> entry.getProfile().getId().equals(player.getUniqueId()))
                        .findFirst()
                        .ifPresent(entry -> {
                            originalEntries.put(player.getUniqueId(), entry);
                            System.out.println("Join: Found gamemode for " + player.getUsername() + ": " + entry.getGameMode());
                        });
                });
                
                server.getAllPlayers().forEach(viewer -> 
                    updateTabListForPlayer(viewer, originalEntries));
            })
            .delay(1500, TimeUnit.MILLISECONDS)
            .schedule();
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        // 延迟更新其他玩家的 TabList
        server.getScheduler()
            .buildTask(plugin, () -> {
                Map<UUID, TabListEntry> originalEntries = new HashMap<>();
                
                // 从所有玩家的 TabList 中收集信息
                server.getAllPlayers().forEach(p -> {
                    p.getTabList().getEntries().stream()
                        .filter(entry -> entry.getProfile().getId().equals(player.getUniqueId()))
                        .findFirst()
                        .ifPresent(entry -> {
                            originalEntries.put(player.getUniqueId(), entry);
                            System.out.println("Disconnect: Found gamemode for " + player.getUsername() + ": " + entry.getGameMode());
                        });
                });
                
                server.getAllPlayers().stream()
                    .filter(p -> !p.equals(player))
                    .forEach(viewer -> updateTabListForPlayer(viewer, originalEntries));
            })
            .delay(1500, TimeUnit.MILLISECONDS)
            .schedule();
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        
        // 获取玩家的服务器连接
        player.getCurrentServer().ifPresent(serverConnection -> {
            // 延迟获取游戏模式信息，增加延迟到 1500ms 以确保后端服务器有足够时间发送游戏模式
            server.getScheduler()
                .buildTask(plugin, () -> {
                    Map<UUID, TabListEntry> originalEntries = new HashMap<>();
                    
                    // 从所有玩家的 TabList 中收集信息
                    server.getAllPlayers().forEach(p -> {
                        p.getTabList().getEntries().stream()
                            .filter(entry -> entry.getProfile().getId().equals(player.getUniqueId()))
                            .findFirst()
                            .ifPresent(entry -> {
                                // 保存所有游戏模式信息
                                originalEntries.put(player.getUniqueId(), entry);
                                // 记录日志以便调试
                                System.out.println("Found gamemode for " + player.getUsername() + ": " + entry.getGameMode());
                            });
                    });
                    
                    // 如果没有获取到游戏模式信息，再等待一段时间
                    if (originalEntries.isEmpty()) {
                        server.getScheduler()
                            .buildTask(plugin, () -> {
                                Map<UUID, TabListEntry> retryEntries = new HashMap<>();
                                server.getAllPlayers().forEach(p -> {
                                    p.getTabList().getEntries().stream()
                                        .filter(entry -> entry.getProfile().getId().equals(player.getUniqueId()))
                                        .findFirst()
                                        .ifPresent(entry -> {
                                            retryEntries.put(player.getUniqueId(), entry);
                                            // 记录重试日志
                                            System.out.println("Retry: Found gamemode for " + player.getUsername() + ": " + entry.getGameMode());
                                        });
                                });
                                server.getAllPlayers().forEach(viewer -> 
                                    updateTabListForPlayer(viewer, retryEntries));
                            })
                            .delay(500, TimeUnit.MILLISECONDS)
                            .schedule();
                    } else {
                        server.getAllPlayers().forEach(viewer -> 
                            updateTabListForPlayer(viewer, originalEntries));
                    }
                })
                .delay(1500, TimeUnit.MILLISECONDS)  // 增加延迟到 1500ms
                .schedule();
        });
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