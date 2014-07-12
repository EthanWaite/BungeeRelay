package io.github.dead_i.bungeerelay.commands;

import io.github.dead_i.bungeerelay.BungeeRelay;
import io.github.dead_i.bungeerelay.IRC;
import io.github.dead_i.bungeerelay.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class PMReplyCommand extends Command {
    Plugin plugin;
    public PMReplyCommand(Plugin plugin) {
        super("pmreply", "irc.pm");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /pmreply <message ...>"));
            return;
        }
        if (!IRC.sock.isConnected()) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "The proxy is not connected to IRC."));
            return;
        }
        if (!IRC.replies.containsKey(sender)) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "You must be engaged within a conversation to use this."));
            return;
        }
        String to = IRC.replies.get(sender);
        String uid = Util.getUidByNick(to);
        if (uid == null) {
            sender.sendMessage(new TextComponent(ChatColor.RED + to + " is no longer on IRC."));
            return;
        }

        StringBuilder msg = new StringBuilder();
        for (String a : args) msg.append(a).append(" ");

        IRC.out.println(":" + IRC.uids.get(sender) + " PRIVMSG " + uid + " :" + msg);
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', BungeeRelay.getConfig().getString("formats.privatemsg")
                .replace("{SENDER}", sender.getName())
                .replace("{MESSAGE}", msg.toString().trim()))));
    }
}
