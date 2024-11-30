package com.example.velocityplugin;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import com.example.velocityplugin.config.TabPreConfig;
import com.example.velocityplugin.commands.TabPreCommand;
import com.example.velocityplugin.listeners.TabListListener;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(
    id = "tabpre",
    name = "TabPre",
    version = "1.0.0",
    description = "一个用于自定义玩家Tab列表前缀的Velocity插件",
    authors = {"Your Organization"}
)
public class TabPrePlugin {
    private final ProxyServer server;
    private final Logger logger;
    private final TabPreConfig config;
    private final Path dataDirectory;
    private TabListListener tabListListener;

    @Inject
    public TabPrePlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.config = new TabPreConfig(dataDirectory, logger);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            // 加载配置
            config.load();
            
            // 创建并注册事件监听器，传入 this 作为插件实例
            tabListListener = new TabListListener(config, server, this);
            server.getEventManager().register(this, tabListListener);
            
            // 注册命令
            server.getCommandManager().register("tabprefix", new TabPreCommand(config, tabListListener));
            
            logger.info("TabPre插件已成功启动！");
        } catch (IOException e) {
            logger.error("插件启动失败！无法加载配置文件", e);
        }
    }
} 