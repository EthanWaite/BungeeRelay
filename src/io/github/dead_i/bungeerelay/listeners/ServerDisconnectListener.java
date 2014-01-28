package io.github.dead_i.bungeerelay.listeners;

import io.github.dead_i.bungeerelay.IRC;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class ServerDisconnectListener implements Listener {
    Plugin plugin;
    public ServerDisconnectListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerDisconnect(ServerDisconnectEvent event) {
        if (IRC.config.getString("server.channel").isEmpty()) IRC.out.println(":" + IRC.uids.get(event.getPlayer()) + " PART " + IRC.config.getString("server.chanprefix") + event.getTarget().getName() + " :Left the server");
    }
}
