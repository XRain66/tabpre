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
            sendMessage(source, config.getMessage("help-message"));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!source.hasPermission("tabpre.reload")) {
                    sendMessage(source, config.getMessage("no-permission"));
                    return;
                }
                try {
                    config.load();
                    tabListListener.refreshAllPlayers();
                    sendMessage(source, config.getMessage("reload-success"));
                } catch (IOException e) {
                    sendMessage(source, "&c配置重载失败！请检查控制台获取详细信息。");
                }
                break;
            case "debug":
                if (args.length < 2) {
                    sendMessage(source, "&c用法: /tabprefix debug <玩家名字>");
                    return;
                }
                String playerName = args[1];
                server.getPlayer(playerName).ifPresentOrElse(
                    player -> {
                        // 使用 getEntries() 获取所有条目
                        Optional<TabListEntry> entry = player.getTabList().getEntries().stream()
                            .filter(e -> e.getProfile().getId().equals(player.getUniqueId()))
                            .findFirst();
                        
                        if (entry.isPresent()) {
                            TabListEntry tabEntry = entry.get();
                            sendMessage(source, "&6=== TabList Debug 信息 ===");
                            sendMessage(source, "&e玩家名: &f" + player.getUsername());
                            // 使用 Component 的 toString()
                            sendMessage(source, "&e显示名称: &f" + tabEntry.getDisplayNameComponent().toString());
                            sendMessage(source, "&e游戏模式: &f" + tabEntry.getGameMode());
                            sendMessage(source, "&e延迟: &f" + tabEntry.getLatency());
                            sendMessage(source, "&e UUID: &f" + tabEntry.getProfile().getId());
                        } else {
                            sendMessage(source, "&c无法获取玩家的 TabList 信息");
                        }
                    },
                    () -> sendMessage(source, "&c找不到玩家: " + playerName)
                );
                break;
            case "help":
            default:
                sendMessage(source, config.getMessage("help-message"));
                break;
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> suggestions = new ArrayList<>();
        String[] args = invocation.arguments();

        if (args.length == 1) {
            if ("reload".startsWith(args[0].toLowerCase()) && invocation.source().hasPermission("tabpre.reload")) {
                suggestions.add("reload");
            }
            if ("help".startsWith(args[0].toLowerCase())) {
                suggestions.add("help");
            }
        }

        return suggestions;
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(suggest(invocation));
    }

    private void sendMessage(CommandSource source, String message) {
        if (message == null || message.isEmpty()) return;
        source.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }
} 