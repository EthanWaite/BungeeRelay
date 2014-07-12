package io.github.dead_i.bungeerelay.commands;

import io.github.dead_i.bungeerelay.BungeeRelay;
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

public class PMCommand extends Command {
    Plugin plugin;
    public PMCommand(Plugin plugin) {
        super("pm", "irc.pm");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /pm <user> <message ...>"));
            return;
        }
        if (!IRC.sock.isConnected()) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "The proxy is not connected to IRC."));
            return;
        }
        String uid = Util.getUidByNick(args[0]);
        if (uid == null) {
            sender.sendMessage(new TextComponent(ChatColor.RED + args[0] + " is not on IRC right now."));
            return;
        }
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(args));
        list.remove(0);
        StringBuilder msg = new StringBuilder();
        for (String a : list) msg.append(a).append(" ");
        IRC.out.println(":" + IRC.uids.get(sender) + " PRIVMSG " + uid + " :" + msg);
        IRC.replies.put((ProxiedPlayer) sender, args[0]);
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', BungeeRelay.getConfig().getString("formats.privatemsg")
                .replace("{SENDER}", sender.getName())
                .replace("{MESSAGE}", msg))));
    }
}
