package com.example.velocityplugin.config;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TabPreConfig {
    private final Path dataDirectory;
    private final Logger logger;
    private CommentedConfigurationNode config;
    private final Map<String, String> playerPrefixes = new HashMap<>();
    private static final int CURRENT_VERSION = 1;

    @Inject
    public TabPreConfig(@DataDirectory Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    public void load() throws IOException {
        // 确保配置目录存在
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory);
        }

        Path configPath = dataDirectory.resolve("config.yml");
        
        // 如果配置文件不存在，从资源中复制
        if (!Files.exists(configPath)) {
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                if (in != null) {
                    Files.copy(in, configPath);
                } else {
                    logger.error("无法找到默认配置文件！");
                    return;
                }
            }
        }

        // 加载配置
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
            .path(configPath)
            .build();
            
        config = loader.load();

        // 检查配置版本
        int version = config.node("version").getInt(0);
        if (version < CURRENT_VERSION) {
            logger.warn("配置文件版本过低！请备份当前配置文件并删除，让插件生成新的配置文件。");
        }

        // 加载前缀
        loadPrefixes();
    }

    private void loadPrefixes() {
        playerPrefixes.clear();
        CommentedConfigurationNode prefixesNode = config.node("prefixes");
        prefixesNode.childrenMap().forEach((key, value) -> 
            playerPrefixes.put(String.valueOf(key).toLowerCase(), 
                             String.valueOf(value.raw())));
    }

    public String getPrefix(String playerName) {
        return playerPrefixes.get(playerName.toLowerCase());
    }

    public String getMessage(String key) {
        return config.node("messages", key).getString("");
    }

    public boolean hasPrefix(String playerName) {
        return playerPrefixes.containsKey(playerName.toLowerCase());
    }
} 