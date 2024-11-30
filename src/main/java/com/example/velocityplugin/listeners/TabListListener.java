package com.example.velocityplugin.listeners;

import com.example.velocityplugin.config.TabPreConfig;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerListUpdateEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class TabListListener {
    private final TabPreConfig config;

    public TabListListener(TabPreConfig config) {
        this.config = config;
    }

    @Subscribe
    public void onPlayerListUpdate(PlayerListUpdateEvent event) {
        event.getTabList().getEntries().forEach(entry -> {
            String playerName = entry.getProfile().getName();
            if (config.hasPrefix(playerName)) {
                String prefix = config.getPrefix(playerName);
                entry.setDisplayName(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(prefix + playerName));
            }
        });
    }
} 