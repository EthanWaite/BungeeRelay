package io.github.dead_i.bungeerelay.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class PMRCommand extends Command {
    Plugin plugin;
    public PMRCommand(Plugin plugin) {
        super("pmr", "irc.pm");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        new PMReplyCommand(plugin).execute(sender, args);
    }
}
