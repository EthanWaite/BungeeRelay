package io.github.dead_i.bungeerelay.commands;

import io.github.dead_i.bungeerelay.IRC;
import io.github.dead_i.bungeerelay.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;

public class KickCommand extends Command {
    Plugin plugin;
    public KickCommand(Plugin plugin) {
        super("irckick", "irc.kick");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /irckick <user> [message ...]"));
            return;
        }
        if (!IRC.sock.isConnected()) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "The proxy is not connected to IRC."));
            return;
        }

        String uid = Util.getUidByNick(args[0]);
        String chan = IRC.config.getString("server.channel");
        if (chan.isEmpty()) chan = IRC.config.getString("server.chanprefix") + ((ProxiedPlayer) sender).getServer().getInfo().getName();
        ArrayList<String> chanusers = IRC.chans.get(chan).users;
        if (!chanusers.contains(uid)) {
            sender.sendMessage(new TextComponent(ChatColor.RED + args[0] + " is not in this IRC channel."));
            return;
        }

        String msg;
        if (args.length == 1) {
            msg = IRC.config.getString("formats.defaultkick");
        }else{
            ArrayList<String> list = new ArrayList<String>(Arrays.asList(args));
            list.remove(0);
            StringBuilder sb = new StringBuilder();
            for (String a : list) sb.append(a).append(" ");
            msg = sb.toString().trim();
        }

        IRC.out.println(":" + IRC.uids.get(sender) + " KICK " + chan + " " + uid + " :" + msg);
        IRC.chans.get(chan).users.remove(uid);
        for (ProxiedPlayer p : Util.getPlayersByChannel(chan)) {
            p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', IRC.config.getString("formats.kick")
                    .replace("{SENDER}", sender.getName())
                    .replace("{TARGET}", args[0])
                    .replace("{REASON}", msg))));
        }
    }
}
