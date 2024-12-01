package com.example.velocityplugin.config;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TabPreConfig {
    private final Path dataDirectory;
    private final Logger logger;
    private CommentedConfigurationNode config;
    private final Map<String, String> playerPrefixes = new ConcurrentHashMap<>();
    private static final int CURRENT_VERSION = 1;
    private final Path configPath;

    @Inject
    public TabPreConfig(@DataDirectory Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.configPath = dataDirectory.resolve("config.yml");
    }

    public void load() throws IOException {
        // 确保配置目录存在
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory);
        }
        
        // 如果配置文件不存在，从资源中复制
        if (!Files.exists(configPath)) {
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                if (in != null) {
                    Files.copy(in, configPath);
                } else {
                    logger.error("无法找到默认配置文件！");
                    throw new IOException("默认配置文件不存在");
                }
            }
        }

        // 加载配置
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
            .path(configPath)
            .build();
            
        config = loader.load();

        // 验证配置
        validateConfig();

        // 加载前缀
        loadPrefixes();
        
        logger.info("配置加载成功！");
    }
    
    public void save() throws IOException {
        if (config == null) {
            logger.error("无法保存配置：配置未加载");
            return;
        }
        
        try {
            // 更新前缀到配置
            CommentedConfigurationNode prefixesNode = config.node("prefixes");
            prefixesNode.set(new HashMap<>(playerPrefixes));
            
            // 保存配置
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(configPath)
                .build();
                
            loader.save(config);
            logger.info("配置保存成功！");
        } catch (SerializationException e) {
            throw new IOException("保存配置时发生错误", e);
        }
    }

    private void validateConfig() throws IOException {
        try {
            // 检查配置版本
            int version = config.node("version").getInt(0);
            if (version < CURRENT_VERSION) {
                logger.warn("配置文件版本过低！正在尝试升级...");
                upgradeConfig(version);
            }
            
            // 确保必要的节点存在
            ensureNode("messages");
            ensureNode("prefixes");
            
            // 保存可能的更改
            save();
        } catch (SerializationException e) {
            throw new IOException("验证配置时发生错误", e);
        }
    }
    
    private void upgradeConfig(int oldVersion) throws IOException {
        try {
            // 在这里添加配置升级逻辑
            config.node("version").set(CURRENT_VERSION);
            logger.info("配置已升级到版本 {}", CURRENT_VERSION);
        } catch (SerializationException e) {
            throw new IOException("配置升级失败", e);
        }
    }
    
    private void ensureNode(String path) throws IOException {
        if (config.node(path).empty()) {
            try {
                config.node(path).set(new HashMap<>());
                logger.warn("创建缺失的配置节点: {}", path);
            } catch (SerializationException e) {
                throw new IOException("创建配置节点失败: " + path, e);
            }
        }
    }

    private void loadPrefixes() throws IOException {
        try {
            playerPrefixes.clear();
            CommentedConfigurationNode prefixesNode = config.node("prefixes");
            prefixesNode.childrenMap().forEach((key, value) -> {
                String playerName = String.valueOf(key).toLowerCase();
                String prefix = String.valueOf(value.raw());
                if (prefix != null && !prefix.isEmpty()) {
                    playerPrefixes.put(playerName, prefix);
                    logger.debug("加载前缀: {} -> {}", playerName, prefix);
                }
            });
        } catch (Exception e) {
            throw new IOException("加载前缀时发生错误", e);
        }
    }

    public String getPrefix(String playerName) {
        return playerPrefixes.get(playerName.toLowerCase());
    }

    public void setPrefix(String playerName, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            playerPrefixes.remove(playerName.toLowerCase());
        } else {
            playerPrefixes.put(playerName.toLowerCase(), prefix);
        }
    }

    public String getMessage(String key) {
        try {
            String message = config.node("messages", key).getString("");
            if (message.isEmpty()) {
                logger.warn("未找到消息键: {}", key);
            }
            return message;
        } catch (Exception e) {
            logger.error("获取消息时发生错误: {}", key, e);
            return "";
        }
    }

    public boolean hasPrefix(String playerName) {
        return playerPrefixes.containsKey(playerName.toLowerCase());
    }
    
    public Map<String, String> getAllPrefixes() {
        return new HashMap<>(playerPrefixes);
    }
} 