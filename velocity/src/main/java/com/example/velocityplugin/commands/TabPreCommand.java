package com.example.velocityplugin.commands;

import com.example.velocityplugin.config.TabPreConfig;
import com.example.velocityplugin.listeners.TabListListener;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.TabListEntry;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TabPreCommand implements SimpleCommand {
    private final TabPreConfig config;
    private final TabListListener tabListListener;
    private final ProxyServer server;
    private static final String RELOAD_PERMISSION = "tabpre.reload";
    private static final String DEBUG_PERMISSION = "tabpre.debug";

    public TabPreCommand(TabPreConfig config, TabListListener tabListListener, ProxyServer server) {
        this.config = config;
        this.tabListListener = tabListListener;
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            showHelp(source);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(source);
                break;
            case "debug":
                handleDebug(source, args);
                break;
            case "help":
                showHelp(source);
                break;
            default:
                sendMessage(source, config.getMessage("unknown-command"));
                break;
        }
    }
    
    private void handleReload(CommandSource source) {
        if (!source.hasPermission(RELOAD_PERMISSION)) {
            sendMessage(source, config.getMessage("no-permission"));
            return;
        }
        
        try {
            config.load();
            config.save();
            tabListListener.refreshAllPlayers();
            sendMessage(source, config.getMessage("reload-success"));
        } catch (IOException e) {
            sendMessage(source, "&c配置重载失败！请检查控制台获取详细信息。");
        }
    }
    
    private void handleDebug(CommandSource source, String[] args) {
        if (!source.hasPermission(DEBUG_PERMISSION)) {
            sendMessage(source, config.getMessage("no-permission"));
            return;
        }
        
        if (args.length < 2) {
            sendMessage(source, "&c用法: /tabprefix debug <玩家名字>");
            return;
        }
        
        String playerName = args[1];
        server.getPlayer(playerName).ifPresentOrElse(
            player -> {
                Optional<TabListEntry> entry = player.getTabList().getEntries().stream()
                    .filter(e -> e.getProfile().getId().equals(player.getUniqueId()))
                    .findFirst();
                
                if (entry.isPresent()) {
                    TabListEntry tabEntry = entry.get();
                    sendMessage(source, "&6=== TabList Debug 信息 ===");
                    sendMessage(source, "&e玩家名: &f" + player.getUsername());
                    sendMessage(source, "&e显示名称: &f" + tabEntry.getDisplayNameComponent().toString());
                    sendMessage(source, "&e游戏模式: &f" + tabEntry.getGameMode());
                    sendMessage(source, "&e延迟: &f" + tabEntry.getLatency());
                    sendMessage(source, "&e UUID: &f" + tabEntry.getProfile().getId());
                    sendMessage(source, "&e前缀: &f" + (config.hasPrefix(player.getUsername()) ? 
                        config.getPrefix(player.getUsername()) : "无"));
                } else {
                    sendMessage(source, "&c无法获取玩家的 TabList 信息");
                }
            },
            () -> sendMessage(source, "&c找不到玩家: " + playerName)
        );
    }
    
    private void showHelp(CommandSource source) {
        sendMessage(source, "&6=== TabPre 命令帮助 ===");
        if (source.hasPermission(RELOAD_PERMISSION)) {
            sendMessage(source, "&e/tabprefix reload &7- 重新加载配置");
        }
        if (source.hasPermission(DEBUG_PERMISSION)) {
            sendMessage(source, "&e/tabprefix debug <玩家> &7- 显示玩家的 TabList 信息");
        }
        sendMessage(source, "&e/tabprefix help &7- 显示此帮助信息");
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> suggestions = new ArrayList<>();
        String[] args = invocation.arguments();

        if (args.length == 1) {
            if (invocation.source().hasPermission(RELOAD_PERMISSION) && 
                "reload".startsWith(args[0].toLowerCase())) {
                suggestions.add("reload");
            }
            if (invocation.source().hasPermission(DEBUG_PERMISSION) && 
                "debug".startsWith(args[0].toLowerCase())) {
                suggestions.add("debug");
            }
            if ("help".startsWith(args[0].toLowerCase())) {
                suggestions.add("help");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("debug") && 
                   invocation.source().hasPermission(DEBUG_PERMISSION)) {
            server.getAllPlayers().forEach(player -> {
                if (player.getUsername().toLowerCase().startsWith(args[1].toLowerCase())) {
                    suggestions.add(player.getUsername());
                }
            });
        }

        return suggestions;
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(suggest(invocation));
    }

    private void sendMessage(CommandSource source, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }
} 