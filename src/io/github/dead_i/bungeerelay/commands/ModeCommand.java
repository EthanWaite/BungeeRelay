package io.github.dead_i.bungeerelay.commands;

import io.github.dead_i.bungeerelay.IRC;
import io.github.dead_i.bungeerelay.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;

public class ModeCommand extends Command {
    Plugin plugin;
    public ModeCommand(Plugin plugin) {
        super("mode", "irc.mode");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /mode <channel> <mode ...>"));
            return;
        }
        if (!IRC.sock.isConnected()) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "The proxy is not connected to IRC."));
            return;
        }
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(args));
        list.remove(0);
        StringBuilder msg = new StringBuilder();
        for (String a : list) msg.append(a).append(" ");
        if (!Util.giveChannelModes(args[0], msg.toString())) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "One or more of the modes were not recognised."));
            return;
        }
        sender.sendMessage(new TextComponent(ChatColor.GRAY + "Modes set."));
    }
}
