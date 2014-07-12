package io.github.dead_i.bungeerelay.listeners;

import io.github.dead_i.bungeerelay.BungeeRelay;
import io.github.dead_i.bungeerelay.IRC;
import io.github.dead_i.bungeerelay.Util;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class PostLoginListener implements Listener {
    Plugin plugin;
    public PostLoginListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        if (!IRC.sock.isConnected()) return;
        ProxiedPlayer p = event.getPlayer();
        Util.sendUserConnect(p);
        Util.incrementUid();
        String chan = BungeeRelay.getConfig().getString("server.channel");
        if (!chan.isEmpty()) Util.sendChannelJoin(p, chan);
    }
}
