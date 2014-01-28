package io.github.dead_i.bungeerelay.commands;

import io.github.dead_i.bungeerelay.IRC;
import io.github.dead_i.bungeerelay.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class SayCommand extends Command {
    Plugin plugin;
    public SayCommand(Plugin plugin) {
        super("say", "irc.say");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /say <message ...>"));
            return;
        }
        StringBuilder msg = new StringBuilder();
        for (String a : args) msg.append(a);
        plugin.getProxy().broadcast(new TextComponent(ChatColor.translateAlternateColorCodes('&', IRC.config.getString("formats.saycommand").replace("{MESSAGE}", msg.toString()))));
        if (!IRC.sock.isConnected()) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "The proxy is not connected to IRC."));
            return;
        }
        for (String c : Util.getChannels()) IRC.out.println(":" + IRC.mainUid + " PRIVMSG " + c + " :" + msg.toString());
    }
}
