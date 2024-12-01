package com.example.velocityplugin;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
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
    version = "${version}",
    description = "一个用于自定义玩家Tab列表前缀的Velocity插件",
    authors = {"XRain666"}
)
public class TabPrePlugin {
    private final ProxyServer server;
    private final Logger logger;
    private final TabPreConfig config;
    private final Path dataDirectory;
    private TabListListener tabListListener;
    private boolean isEnabled = false;

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
            
            // 创建并注册事件监听器
            tabListListener = new TabListListener(config, server, this);
            server.getEventManager().register(this, tabListListener);
            
            // 注册命令
            server.getCommandManager().register("tabprefix", 
                new TabPreCommand(config, tabListListener, server));
            
            isEnabled = true;
            logger.info("TabPre插件已成功启动！");
        } catch (Exception e) {
            logger.error("插件启动失败！", e);
            tryRecovery();
        }
    }
    
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (isEnabled) {
            try {
                // 保存配置
                config.save();
                
                // 注销事件监听器
                if (tabListListener != null) {
                    tabListListener.shutdown();
                    server.getEventManager().unregisterListener(this, tabListListener);
                }
                
                // 注销命令
                server.getCommandManager().unregister("tabprefix");
                
                logger.info("TabPre插件已成功卸载！");
            } catch (Exception e) {
                logger.error("插件卸载时发生错误！", e);
            }
        }
    }
    
    private void tryRecovery() {
        try {
            // 尝试重新加载配置
            config.load();
            logger.info("配置重新加载成功");
            
            // 如果监听器存在，尝试重新注册
            if (tabListListener != null) {
                server.getEventManager().unregisterListener(this, tabListListener);
                tabListListener = new TabListListener(config, server, this);
                server.getEventManager().register(this, tabListListener);
                logger.info("事件监听器重新注册成功");
            }
            
            isEnabled = true;
            logger.info("插件恢复成功！");
        } catch (Exception e) {
            logger.error("插件恢复失败！", e);
            isEnabled = false;
        }
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }
} 