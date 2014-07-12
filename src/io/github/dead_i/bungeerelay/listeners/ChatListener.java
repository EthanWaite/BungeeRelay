package io.github.dead_i.bungeerelay.listeners;

import io.github.dead_i.bungeerelay.BungeeRelay;
import io.github.dead_i.bungeerelay.IRC;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class ChatListener implements Listener {
    Plugin plugin;
    public ChatListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        if (!IRC.sock.isConnected()) return;
        ProxiedPlayer p = (ProxiedPlayer) event.getSender();
        String msg = event.getMessage();
        if (!msg.startsWith("/") || (msg.startsWith("/me") && p.hasPermission("irc.me"))) {
            String chanconf = BungeeRelay.getConfig().getString("server.channel");
            String channel = chanconf;
            if (channel.isEmpty()) channel = BungeeRelay.getConfig().getString("server.chanprefix") + p.getServer().getInfo().getName();
            if (msg.startsWith("/me ")) msg = (char) 1 + "ACTION " + msg;
            IRC.out.println(":" + IRC.uids.get(p) + " PRIVMSG " + channel + " :" + msg);

            if (!chanconf.isEmpty()) for (ProxiedPlayer o : plugin.getProxy().getPlayers()) {
                if (!p.getServer().getInfo().getName().equals(o.getServer().getInfo().getName())) o.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', BungeeRelay.getConfig().getString("formats.msg"))
                        .replace("{SENDER}", p.getName())
                        .replace("{MESSAGE}", msg)));
            }
        }
    }
}
