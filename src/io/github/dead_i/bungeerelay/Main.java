package io.github.dead_i.bungeerelay;

import io.github.dead_i.bungeerelay.commands.*;
import io.github.dead_i.bungeerelay.listeners.*;
import net.craftminecraft.bungee.bungeeyaml.pluginapi.ConfigurablePlugin;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class Main extends ConfigurablePlugin {
    public void onEnable() {
        // Immediately provide an offline mode warning, due to how incredibly dangerous it is in the case of this plugin.
        if (!getProxy().getConfig().isOnlineMode()) {
            getLogger().warning("IMPORTANT! BungeeCord is running offline mode, meaning hackers and cracked accounts can log in. This means that ANYONE could gain power on your IRC server, such as IRC oper, by impersonating you. It is highly recommended that you turn online-mode to TRUE in your BungeeCord config.yml!");
            getLogger().warning("----------------------------------------------------------");
            getLogger().warning("Remember - only your Bukkit/Spigot servers require online-mode=false");
            getLogger().warning("In BungeeCord, however, online-mode should be set to true.");
        }

        // Save the default configuration
        saveDefaultConfig();

        // Register listeners
        getProxy().getPluginManager().registerListener(this, new ChatListener(this));
        getProxy().getPluginManager().registerListener(this, new PlayerDisconnectListener(this));
        getProxy().getPluginManager().registerListener(this, new PostLoginListener(this));
        getProxy().getPluginManager().registerListener(this, new ServerConnectListener(this));
        getProxy().getPluginManager().registerListener(this, new ServerDisconnectListener(this));

        // Register commands
        getProxy().getPluginManager().registerCommand(this, new SayCommand(this));
        getProxy().getPluginManager().registerCommand(this, new ModeCommand(this));
        getProxy().getPluginManager().registerCommand(this, new PMCommand(this));
        getProxy().getPluginManager().registerCommand(this, new PMReplyCommand(this));
        getProxy().getPluginManager().registerCommand(this, new KickCommand(this));

        // Register aliases
        getProxy().getPluginManager().registerCommand(this, new PMRCommand(this));

        // Initiate the connection, which will, in turn, pass the socket to the IRC class
        getProxy().getScheduler().runAsync(this, new Runnable() {
            public void run() {
                connect();
            }
        });
    }

    public void connect() {
        getLogger().info("Attempting connection...");
        try {
            new IRC(new Socket(getConfig().getString("server.host"), getConfig().getInt("server.port")), getConfig(), this);
        } catch (UnknownHostException e) {
            handleDisconnect();
        } catch (IOException e) {
            handleDisconnect();
        }
    }

    public void handleDisconnect() {
        getLogger().info("Disconnected from server.");
        int reconnect = getConfig().getInt("server.reconnect");
        if (reconnect > -1) {
            getLogger().info("Reconnecting in " + reconnect / 1000 + " seconds...");
            getProxy().getScheduler().schedule(this, new Runnable() {
                @Override
                public void run() {
                    connect();
                }
            }, reconnect, TimeUnit.MILLISECONDS);
        }
    }
}