package com.example.velocityplugin.listeners;

import com.example.velocityplugin.TabPrePlugin;
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
import java.util.concurrent.ScheduledFuture;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.google.common.io.ByteStreams;
import com.google.common.io.ByteArrayDataInput;
import org.slf4j.Logger;

public class TabListListener {
    private final TabPreConfig config;
    private final ProxyServer server;
    private final TabPrePlugin plugin;
    private final Logger logger;
    private ScheduledFuture<?> updateTask;
    private static final MinecraftChannelIdentifier GAMEMODE_CHANNEL = 
        MinecraftChannelIdentifier.create("tabpre", "gamemode");

    public TabListListener(TabPreConfig config, ProxyServer server, TabPrePlugin plugin) {
        this.config = config;
        this.server = server;
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        
        // 注册插件消息通道
        server.getChannelRegistrar().register(GAMEMODE_CHANNEL);
        
        // 监听游戏模式变更消息
        server.getEventManager().register(plugin, PluginMessageEvent.class, this::handlePluginMessage);
        
        // 启动定时更新任务
        startPeriodicUpdate();
    }
    
    private void handlePluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(GAMEMODE_CHANNEL)) {
            return;
        }
        
        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
            String playerName = in.readUTF();
            int newGameMode = in.readInt();
            
            server.getPlayer(playerName).ifPresent(player -> {
                logger.debug("收到游戏模式变更消息: {} -> {}", playerName, newGameMode);
                Map<UUID, Integer> gameModes = new HashMap<>();
                gameModes.put(player.getUniqueId(), newGameMode);
                updateAllPlayers(gameModes);
            });
        } catch (Exception e) {
            logger.error("处理游戏模式变更消息时发生错误: {}", e.getMessage());
        }
    }

    private void startPeriodicUpdate() {
        updateTask = server.getScheduler()
            .buildTask(plugin, this::updateAllPlayersTask)
            .repeat(10L, TimeUnit.SECONDS)
            .schedule();
    }
    
    public void shutdown() {
        if (updateTask != null) {
            updateTask.cancel(false);
        }
        server.getChannelRegistrar().unregister(GAMEMODE_CHANNEL);
    }

    private void updateAllPlayersTask() {
        try {
            Map<UUID, Integer> gameModes = collectGameModes();
            if (!gameModes.isEmpty()) {
                updateAllPlayers(gameModes);
            }
        } catch (Exception e) {
            logger.error("定时更新任务执行失败: {}", e.getMessage());
        }
    }
    
    private Map<UUID, Integer> collectGameModes() {
        Map<UUID, Integer> gameModes = new HashMap<>();
        server.getAllPlayers().forEach(player -> {
            player.getCurrentServer().ifPresent(serverConnection -> {
                String serverName = serverConnection.getServerInfo().getName();
                logger.debug("检查玩家 {} 在服务器 {} 的状态", player.getUsername(), serverName);
                
                findPlayerTabListEntry(player).ifPresent(entry -> {
                    int gameMode = entry.getGameMode();
                    gameModes.put(player.getUniqueId(), gameMode);
                    logger.debug("获取到玩家 {} 的游戏模式: {}", player.getUsername(), gameMode);
                });
            });
        });
        return gameModes;
    }

    private void updateAllPlayers(Map<UUID, Integer> gameModes) {
        server.getAllPlayers().forEach(viewer -> {
            try {
                updateTabListForPlayer(viewer, gameModes);
            } catch (Exception e) {
                logger.error("更新玩家 {} 的 TabList 时发生错误: {}", 
                    viewer.getUsername(), e.getMessage());
            }
        });
    }

    private void updateTabListForPlayer(Player viewer, Map<UUID, Integer> gameModes) {
        // 清空现有条目
        clearExistingEntries(viewer);
        
        // 重新添加所有玩家
        for (Player target : server.getAllPlayers()) {
            try {
                addPlayerToTabList(viewer, target, gameModes.getOrDefault(target.getUniqueId(), 0));
            } catch (Exception e) {
                logger.error("向玩家 {} 的 TabList 添加玩家 {} 时发生错误: {}", 
                    viewer.getUsername(), target.getUsername(), e.getMessage());
            }
        }
    }
    
    private void clearExistingEntries(Player viewer) {
        Set<UUID> existingEntries = new HashSet<>();
        viewer.getTabList().getEntries().forEach(entry -> 
            existingEntries.add(entry.getProfile().getId()));
        existingEntries.forEach(uuid -> viewer.getTabList().removeEntry(uuid));
    }
    
    private void addPlayerToTabList(Player viewer, Player target, int gameMode) {
        Component displayName = getDisplayName(target);
        viewer.getTabList().addEntry(TabListEntry.builder()
            .profile(target.getGameProfile())
            .displayName(displayName)
            .tabList(viewer.getTabList())
            .latency((int) target.getPing())
            .gameMode(gameMode)
            .build());
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        Player player = event.getPlayer();
        scheduleInitialUpdate(player);
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        scheduleDisconnectUpdate(player);
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        scheduleServerConnectedUpdate(player);
    }
    
    private void scheduleInitialUpdate(Player player) {
        server.getScheduler()
            .buildTask(plugin, () -> {
                try {
                    Map<UUID, Integer> gameModes = collectGameModes();
                    updateAllPlayers(gameModes);
                } catch (Exception e) {
                    logger.error("玩家 {} 加入时更新 TabList 失败: {}", 
                        player.getUsername(), e.getMessage());
                }
            })
            .delay(1500, TimeUnit.MILLISECONDS)
            .schedule();
    }
    
    private void scheduleDisconnectUpdate(Player player) {
        server.getScheduler()
            .buildTask(plugin, () -> {
                try {
                    Map<UUID, Integer> gameModes = collectGameModes();
                    updateAllPlayers(gameModes);
                } catch (Exception e) {
                    logger.error("玩家 {} 断开连接时更新 TabList 失败: {}", 
                        player.getUsername(), e.getMessage());
                }
            })
            .delay(500, TimeUnit.MILLISECONDS)
            .schedule();
    }
    
    private void scheduleServerConnectedUpdate(Player player) {
        server.getScheduler()
            .buildTask(plugin, () -> {
                try {
                    Map<UUID, Integer> gameModes = collectGameModes();
                    updateAllPlayers(gameModes);
                } catch (Exception e) {
                    logger.error("玩家 {} 连接到服务器时更新 TabList 失败: {}", 
                        player.getUsername(), e.getMessage());
                }
            })
            .delay(1500, TimeUnit.MILLISECONDS)
            .schedule();
    }
    
    private Component getDisplayName(Player player) {
        String prefix = config.getPrefix(player.getUsername());
        String displayName = prefix != null ? prefix + player.getUsername() : player.getUsername();
        return LegacyComponentSerializer.legacyAmpersand().deserialize(displayName);
    }
    
    public void refreshAllPlayers() {
        Map<UUID, Integer> gameModes = collectGameModes();
        updateAllPlayers(gameModes);
    }
} 