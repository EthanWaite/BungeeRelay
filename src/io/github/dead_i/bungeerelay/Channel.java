package io.github.dead_i.bungeerelay;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;

public class Channel {
    public ArrayList<String> bans = new ArrayList<String>();
    public ArrayList<String> users = new ArrayList<String>();
    public long ts;

    public Channel(Long ts) {
        this.ts = ts;
    }

    public boolean isBanned(ProxiedPlayer p) {
        for (String ban : bans) {
            String nick = ban.split("!")[0];
            String ident = ban.split("!")[1].split("@")[0];
            String host = ban.split("@")[1];
            String userhost = p.getAddress().getHostName();
            if ((nick.equalsIgnoreCase(IRC.config.getString("server.userprefix") + p.getName() + IRC.config.getString("server.usersuffix")) || nick.equals("*")) && (host.equals(userhost) || host.equals("*")) && (ident.equalsIgnoreCase(p.getName()) || ident.equals("*"))) return true;
        }
        return false;
    }
}
