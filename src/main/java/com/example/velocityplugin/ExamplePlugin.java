package com.example.velocityplugin;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.player.PlayerListUpdateEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.TabListEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import ninja.leaping.configurate.ConfigurationNode;

@Plugin(
    id = "example_plugin",
    name = "Example Plugin",
    version = "1.0.0",
    description = "一个示例 Velocity 插件",
    authors = {"YourName"}
)
public class ExamplePlugin {
    private final ProxyServer server;
    private final Logger logger;
    private Map<String, String> playerPrefixes;
    private File configFile;
    private ConfigurationNode config;

    @Inject
    public ExamplePlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        this.playerPrefixes = new HashMap<>();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // 加载配置
        loadConfig();
        
        // 注册命令
        server.getCommandManager().register("tabprefix", new SimpleCommand() {
            @Override
            public void execute(final Invocation invocation) {
                CommandSource source = invocation.source();
                if (source.hasPermission("tabprefix.reload")) {
                    loadConfig();
                    source.sendMessage(Component.text("配置已重新加载！").color(NamedTextColor.GREEN));
                } else {
                    source.sendMessage(Component.text("你没有权限执行此命令！").color(NamedTextColor.RED));
                }
            }
        }, "tabreload");
        
        // 注册事件监听器
        server.getEventManager().register(this, this);
        
        logger.info("Tab前缀插件已启动！");
    }

    @Subscribe
    public void onPlayerListUpdate(PlayerListUpdateEvent event) {
        event.getTabList().getEntries().forEach(entry -> {
            String playerName = entry.getProfile().getName();
            if (playerPrefixes.containsKey(playerName.toLowerCase())) {
                String prefix = playerPrefixes.get(playerName.toLowerCase());
                Component newDisplayName = LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(prefix + playerName);
                entry.setDisplayName(newDisplayName);
            }
        });
    }

    private void loadConfig() {
        try {
            // 确保配置文件存在
            File dataFolder = new File("plugins/example_plugin");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            configFile = new File(dataFolder, "config.yml");
            if (!configFile.exists()) {
                try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                    Files.copy(in, configFile.toPath());
                }
            }

            // 加载配置
            config = YAMLConfigurationLoader.builder()
                .setFile(configFile)
                .build()
                .load();

            // 读取前缀配置
            ConfigurationNode prefixesNode = config.getNode("prefixes");
            playerPrefixes.clear();
            prefixesNode.getChildrenMap().forEach((key, value) -> {
                playerPrefixes.put(
                    String.valueOf(key).toLowerCase(),
                    String.valueOf(value.getValue())
                );
            });

        } catch (IOException e) {
            logger.error("无法加载配置文件", e);
        }
    }
} 