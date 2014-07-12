package io.github.dead_i.bungeerelay.listeners;

import io.github.dead_i.bungeerelay.BungeeRelay;
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
        if (IRC.sock.isConnected() && BungeeRelay.getConfig().getString("server.channel").isEmpty()) IRC.out.println(":" + IRC.uids.get(event.getPlayer()) + " PART " + BungeeRelay.getConfig().getString("server.chanprefix") + event.getTarget().getName() + " :Left the server");
    }
}
