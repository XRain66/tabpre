package com.example.velocityplugin.commands;

import com.example.velocityplugin.config.TabPreConfig;
import com.example.velocityplugin.listeners.TabListListener;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TabPreCommand implements SimpleCommand {
    private final TabPreConfig config;
    private final TabListListener tabListListener;

    public TabPreCommand(TabPreConfig config, TabListListener tabListListener) {
        this.config = config;
        this.tabListListener = tabListListener;
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
            case "help":
                sendMessage(source, config.getMessage("help-message"));
                break;
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