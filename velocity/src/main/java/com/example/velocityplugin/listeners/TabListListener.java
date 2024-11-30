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
import com.velocitypowered.api.network.MinecraftChannelIdentifier;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.google.common.io.ByteStreams;
import com.google.common.io.ByteArrayDataInput;

public class TabListListener {
    private final TabPreConfig config;
    private final ProxyServer server;
    private final Object plugin;

    public TabListListener(TabPreConfig config, ProxyServer server, Object plugin) {
        this.config = config;
        this.server = server;
        this.plugin = plugin;
        
        // 注册插件消息通道
        server.getChannelRegistrar().register(MinecraftChannelIdentifier.create("tabpre", "gamemode"));
        
        // 监听游戏模式变更消息
        server.getEventManager().register(plugin, PluginMessageEvent.class, event -> {
            if (event.getIdentifier().equals(MinecraftChannelIdentifier.create("tabpre", "gamemode"))) {
                try {
                    // 处理游戏模式变更消息
                    ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
                    String playerName = in.readUTF();
                    int newGameMode = in.readInt();
                    
                    server.getPlayer(playerName).ifPresent(player -> {
                        System.out.println("Received gamemode change: " + playerName + " -> " + newGameMode);
                        // 更新 TabList
                        Map<UUID, Integer> gameModes = new HashMap<>();
                        gameModes.put(player.getUniqueId(), newGameMode);
                        updateAllPlayers(gameModes);
                    });
                } catch (Exception e) {
                    System.out.println("Error processing gamemode change message: " + e.getMessage());
                }
            }
        });
        
        // 启动定时更新任务
        startPeriodicUpdate();
    }

    private void startPeriodicUpdate() {
        // 每 10 秒更新一次
        server.getScheduler()
            .buildTask(plugin, () -> {
                // 获取所有在线玩家的信息
                Map<UUID, Integer> gameModes = new HashMap<>();
                
                // 遍历所有在线玩家
                server.getAllPlayers().forEach(player -> {
                    // 从玩家当前所在的后端服务器获取信息
                    player.getCurrentServer().ifPresent(serverConnection -> {
                        // 记录服务器名称以便调试
                        String serverName = serverConnection.getServerInfo().getName();
                        System.out.println("Checking player " + player.getUsername() + " on server " + serverName);
                        
                        // 获取原始的 TabList 条目
                        TabListEntry originalEntry = null;
                        for (TabListEntry entry : player.getTabList().getEntries()) {
                            if (entry.getProfile().getId().equals(player.getUniqueId())) {
                                originalEntry = entry;
                                break;
                            }
                        }
                        
                        if (originalEntry != null) {
                            int gameMode = originalEntry.getGameMode();
                            gameModes.put(player.getUniqueId(), gameMode);
                            System.out.println("Found gamemode " + gameMode + " for " + player.getUsername() + 
                                " from server " + serverName);
                        }
                    });
                });
                
                // 更新所有玩家的 TabList
                if (!gameModes.isEmpty()) {
                    server.getAllPlayers().forEach(viewer -> {
                        // 清空现有条目
                        Set<UUID> existingEntries = new HashSet<>();
                        viewer.getTabList().getEntries().forEach(entry -> 
                            existingEntries.add(entry.getProfile().getId()));
                        existingEntries.forEach(uuid -> viewer.getTabList().removeEntry(uuid));
                        
                        // 重新添加所有玩家
                        for (Player target : server.getAllPlayers()) {
                            Component displayName = getDisplayName(target);
                            int gameMode = gameModes.getOrDefault(target.getUniqueId(), 0);
                            
                            viewer.getTabList().addEntry(TabListEntry.builder()
                                .profile(target.getGameProfile())
                                .displayName(displayName)
                                .tabList(viewer.getTabList())
                                .latency((int) target.getPing())
                                .gameMode(gameMode)
                                .build());
                        }
                    });
                }
            })
            .repeat(10L, TimeUnit.SECONDS)
            .schedule();
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        Player player = event.getPlayer();
        // 延迟获取游戏模式信息
        server.getScheduler()
            .buildTask(plugin, () -> {
                Map<UUID, Integer> gameModes = new HashMap<>();
                
                // 从玩家当前所在的后端服务器获取信息
                player.getCurrentServer().ifPresent(serverConnection -> {
                    String serverName = serverConnection.getServerInfo().getName();
                    System.out.println("Join: Checking player " + player.getUsername() + " on server " + serverName);
                    
                    // 获取原始的 TabList 条目
                    TabListEntry originalEntry = null;
                    for (TabListEntry entry : player.getTabList().getEntries()) {
                        if (entry.getProfile().getId().equals(player.getUniqueId())) {
                            originalEntry = entry;
                            break;
                        }
                    }
                    
                    if (originalEntry != null) {
                        int gameMode = originalEntry.getGameMode();
                        gameModes.put(player.getUniqueId(), gameMode);
                        System.out.println("Join: Found gamemode " + gameMode + " for " + player.getUsername() + 
                            " from server " + serverName);
                    }
                });
                
                updateAllPlayers(gameModes);
            })
            .delay(1500, TimeUnit.MILLISECONDS)
            .schedule();
    }

    private void updateAllPlayers(Map<UUID, Integer> gameModes) {
        server.getAllPlayers().forEach(viewer -> {
            // 清空现有条目
            Set<UUID> existingEntries = new HashSet<>();
            viewer.getTabList().getEntries().forEach(entry -> 
                existingEntries.add(entry.getProfile().getId()));
            existingEntries.forEach(uuid -> viewer.getTabList().removeEntry(uuid));
            
            // 重新添加所有玩家
            for (Player target : server.getAllPlayers()) {
                Component displayName = getDisplayName(target);
                int gameMode = gameModes.getOrDefault(target.getUniqueId(), 0);
                
                viewer.getTabList().addEntry(TabListEntry.builder()
                    .profile(target.getGameProfile())
                    .displayName(displayName)
                    .tabList(viewer.getTabList())
                    .latency((int) target.getPing())
                    .gameMode(gameMode)
                    .build());
            }
        });
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
            server.getScheduler()
                .buildTask(plugin, () -> {
                    Map<UUID, TabListEntry> originalEntries = new HashMap<>();
                    
                    // 从后端服务器获取信息
                    serverConnection.getServer().getPlayersConnected().stream()
                        .filter(p -> p.getUniqueId().equals(player.getUniqueId()))
                        .findFirst()
                        .ifPresent(p -> {
                            p.getTabList().getEntries().stream()
                                .filter(entry -> entry.getProfile().getId().equals(player.getUniqueId()))
                                .findFirst()
                                .ifPresent(entry -> {
                                    originalEntries.put(player.getUniqueId(), entry);
                                    System.out.println("Server connected from backend: " + 
                                        player.getUsername() + " on " + 
                                        serverConnection.getServerInfo().getName() + 
                                        " gamemode: " + entry.getGameMode());
                                });
                        });
                    
                    // 如果没有获取到信息，尝试重试
                    if (originalEntries.isEmpty()) {
                        server.getScheduler()
                            .buildTask(plugin, () -> retryGetGameMode(player))
                            .delay(500, TimeUnit.MILLISECONDS)
                            .schedule();
                    } else {
                        server.getAllPlayers().forEach(viewer -> 
                            updateTabListForPlayer(viewer, originalEntries));
                    }
                })
                .delay(1500, TimeUnit.MILLISECONDS)
                .schedule();
        });
    }

    private void retryGetGameMode(Player player) {
        Map<UUID, TabListEntry> retryEntries = new HashMap<>();
        player.getCurrentServer().ifPresent(serverConnection -> {
            serverConnection.getServer().getPlayersConnected().stream()
                .filter(p -> p.getUniqueId().equals(player.getUniqueId()))
                .findFirst()
                .ifPresent(p -> {
                    p.getTabList().getEntries().stream()
                        .filter(entry -> entry.getProfile().getId().equals(player.getUniqueId()))
                        .findFirst()
                        .ifPresent(entry -> {
                            retryEntries.put(player.getUniqueId(), entry);
                            System.out.println("Retry from backend: " + 
                                player.getUsername() + " on " + 
                                serverConnection.getServerInfo().getName() + 
                                " gamemode: " + entry.getGameMode());
                        });
                });
        });
        
        if (!retryEntries.isEmpty()) {
            server.getAllPlayers().forEach(viewer -> 
                updateTabListForPlayer(viewer, retryEntries));
        }
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