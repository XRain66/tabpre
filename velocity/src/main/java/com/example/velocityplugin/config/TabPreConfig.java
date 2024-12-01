package com.example.velocityplugin.config;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TabPreConfig {
    private final Path dataDirectory;
    private final Logger logger;
    private Map<String, String> prefixes;
    private Map<String, String> messages;

    public TabPreConfig(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.prefixes = new HashMap<>();
        this.messages = new HashMap<>();
    }

    public void load() throws IOException {
        // 确保配置目录存在
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory);
        }

        // 配置文件路径
        Path configPath = dataDirectory.resolve("config.yml");

        // 如果配置文件不存在，创建默认配置
        if (!Files.exists(configPath)) {
            createDefaultConfig(configPath);
        }

        // 加载配置
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
            .path(configPath)
            .build();

        CommentedConfigurationNode root = loader.load();
        
        // 加载前缀配置
        prefixes.clear();
        CommentedConfigurationNode prefixesNode = root.node("prefixes");
        if (!prefixesNode.virtual()) {
            Map<Object, ? extends CommentedConfigurationNode> prefixMap = prefixesNode.childrenMap();
            for (Map.Entry<Object, ? extends CommentedConfigurationNode> entry : prefixMap.entrySet()) {
                String playerName = entry.getKey().toString();
                String prefix = entry.getValue().getString("");
                prefixes.put(playerName, prefix);
            }
        }

        // 加载消息配置
        messages.clear();
        CommentedConfigurationNode messagesNode = root.node("messages");
        if (!messagesNode.virtual()) {
            Map<Object, ? extends CommentedConfigurationNode> messageMap = messagesNode.childrenMap();
            for (Map.Entry<Object, ? extends CommentedConfigurationNode> entry : messageMap.entrySet()) {
                String key = entry.getKey().toString();
                String message = entry.getValue().getString("");
                messages.put(key, message);
            }
        }
    }

    private void createDefaultConfig(Path configPath) throws IOException {
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
            .path(configPath)
            .build();

        CommentedConfigurationNode root = loader.createNode();
        
        // 设置版本
        root.node("version").set(1);
        
        // 设置消息
        root.node("messages", "reload-success").set("&a配置重载成功！");
        root.node("messages", "no-permission").set("&c你没有权限执行此命令！");
        root.node("messages", "unknown-command").set("&c未知命令！使用 /tabprefix help 查看帮助。");
        root.node("messages", "help-message").set(
            "&6=== TabPre 命令帮助 ===\n" +
            "&e/tabprefix reload &7- 重新加载配置\n" +
            "&e/tabprefix debug <玩家> &7- 显示玩家的 TabList 信息\n" +
            "&e/tabprefix help &7- 显示此帮助信息"
        );
        
        // 设置示例前缀
        root.node("prefixes", "example").set("&c[管理员] &f");
        
        // 保存配置
        loader.save(root);
    }

    public void save() throws IOException {
        Path configPath = dataDirectory.resolve("config.yml");
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
            .path(configPath)
            .build();

        CommentedConfigurationNode root = loader.createNode();
        
        // 保存前缀配置
        for (Map.Entry<String, String> entry : prefixes.entrySet()) {
            root.node("prefixes", entry.getKey()).set(entry.getValue());
        }

        // 保存消息配置
        for (Map.Entry<String, String> entry : messages.entrySet()) {
            root.node("messages", entry.getKey()).set(entry.getValue());
        }
        
        loader.save(root);
    }

    public Map<String, String> getPrefixes() {
        return prefixes;
    }

    public String getPrefix(String playerName) {
        return prefixes.getOrDefault(playerName, "");
    }

    public void setPrefix(String playerName, String prefix) {
        prefixes.put(playerName, prefix);
    }

    public void removePrefix(String playerName) {
        prefixes.remove(playerName);
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "&c未找到消息: " + key);
    }

    public void setMessage(String key, String message) {
        messages.put(key, message);
    }
} 