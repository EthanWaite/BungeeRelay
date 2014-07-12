package io.github.dead_i.bungeerelay;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;

public class Util {
    private static ProxyServer proxy = ProxyServer.getInstance();

    public static void incrementUid(int pos) {
        StringBuilder sb = new StringBuilder(IRC.currentUid);
        if (IRC.currentUid.charAt(pos) == 'Z') {
            sb.setCharAt(pos, '0');
            IRC.currentUid = sb.toString();
        }else if (IRC.currentUid.charAt(pos) == '9') {
            sb.setCharAt(pos, 'A');
            IRC.currentUid = sb.toString();
            if (pos == 3) return;
            incrementUid(pos - 1);
        }else{
            sb.setCharAt(pos, (char) (IRC.currentUid.charAt(pos) + 1));
            IRC.currentUid = sb.toString();
        }
    }

    public static void incrementUid() {
        incrementUid(8);
    }

    public static void sendUserConnect(ProxiedPlayer p) {
        String name = BungeeRelay.getConfig().getString("server.userprefix") + p.getName() + BungeeRelay.getConfig().getString("server.usersuffix");
        String host = BungeeRelay.getConfig().getString("server.userhost")
                .replace("{UUID}", p.getUniqueId().toString().replace("-", ""))
                .replace("{PLAYER}", p.getName())
                .replace("{HOST}", p.getAddress().getHostString());

        IRC.times.put(p, System.currentTimeMillis() / 1000);
        IRC.uids.put(p, IRC.currentUid);
        IRC.users.put(IRC.currentUid, name);
        IRC.out.println("UID " + IRC.currentUid + " " + System.currentTimeMillis() / 1000 + " " + name + " " + p.getAddress().getHostName() + " " + host + " " + p.getName() + " " + p.getAddress().toString() + " " + IRC.times.get(p) + " +r :Minecraft Player");
    }

    public static void sendChannelJoin(ProxiedPlayer p, String c) {
        String uid = IRC.uids.get(p);
        IRC.out.println("FJOIN " + c + " " + System.currentTimeMillis() / 1000 + " +nt :," + uid);
        giveChannelModes(p, c);
    }

    public static void giveChannelModes(ProxiedPlayer p, String c) {
        String modes = "+";
        if (p.hasPermission("irc.owner")) modes += "";
        if (p.hasPermission("irc.protect")) modes += "a";
        if (p.hasPermission("irc.op")) modes += "o";
        if (p.hasPermission("irc.halfop")) modes += "h";
        if (p.hasPermission("irc.voice")) modes += "v";
        giveChannelModes(c, modes, IRC.uids.get(p));
    }

    public static void giveChannelModes(String c, String m, String s) {
        if (!m.isEmpty()) {
            String target = "";
            for (int i=0; i<m.length(); i++) {
                target += s + " ";
            }
            giveChannelModes(c, "+" + m + " " + target.trim());
        }
    }

    public static boolean giveChannelModes(String c, String m) {
        String prefixModes = IRC.prefixes.split("\\(")[1].split("\\)")[0];
        String modes = m.split(" ")[0];
        for (int i=0; i<modes.length(); i++) {
            String mode = Character.toString(modes.charAt(i));
            if (!prefixModes.contains(mode) && !IRC.chanModes.contains(mode) && !mode.equals("+") && !mode.equals("-")) {
                proxy.getLogger().warning("Tried to set the +" + mode + " mode, but the IRC server stated earlier that it wasn't compatible with this mode.");
                proxy.getLogger().warning("If you want to use " + mode + ", enable appropriate module in your IRC server's configuration files.");
                proxy.getLogger().warning("Skipping...");
                return false;
            }
        }
        IRC.out.println(":" + IRC.mainUid + " FMODE " + c + " " + getChanTS(c) + " " + m);
        return true;
    }

    public static void sendMainJoin(String c, String m, String t) {
        long chanTS = getChanTS(c);
        IRC.out.println("FJOIN " + c + " " + chanTS + " +nt :," + IRC.mainUid);

        giveChannelModes(c, m, IRC.mainUid);

        if (!t.isEmpty()) IRC.out.println(":" + IRC.mainUid + " TOPIC " + c + " :" + t);
    }

    public static List<String> getChannels() {
        List<String> out = new ArrayList<String>();
        String channel = BungeeRelay.getConfig().getString("server.channel");
        if (channel.isEmpty()) {
            for (ServerInfo si : proxy.getServers().values()) {
                out.add(BungeeRelay.getConfig().getString("server.chanprefix") + si.getName());
            }
        }else{
            out.add(channel);
        }
        return out;
    }

    public static Collection<ProxiedPlayer> getPlayersByChannel(String c) {
        if (BungeeRelay.getConfig().getString("server.staff").equalsIgnoreCase(c)) {
            return Collections.emptyList();
        }else if (BungeeRelay.getConfig().getString("server.channel").isEmpty()) {
            String pref = BungeeRelay.getConfig().getString("server.chanprefix");
            if (c.startsWith(pref)) c = c.substring(pref.length());
            ServerInfo si = proxy.getServerInfo(c);
            if (si == null) return Collections.emptyList();
            return si.getPlayers();
        }else{
            return proxy.getPlayers();
        }
    }

    public static String getUidByNick(String nick) {
        for (Map.Entry<String, String> entry : IRC.users.entrySet()) {
            if (nick.equalsIgnoreCase(entry.getValue())) return entry.getKey();
        }
        return null;
    }

    public static Long getChanTS(String c) {
        if (!IRC.chans.containsKey(c)) IRC.chans.put(c, new Channel(System.currentTimeMillis() / 1000));
        return IRC.chans.get(c).ts;
    }

    public static String sliceStringArray(String[] a, Integer l) {
        String s = "";
        for (int i=l; i<a.length; i++) {
            s += a[i] + " ";
        }
        return s.trim();
    }
}
