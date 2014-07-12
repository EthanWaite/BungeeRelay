package io.github.dead_i.bungeerelay;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IRC {
    public static Socket sock;
    public static BufferedReader in;
    public static PrintWriter out;
    public static String mainUid;
    public static String currentUid;
    public static String prefixes;
    public static String chanModes;
    public static long startTime = System.currentTimeMillis() / 1000;
    public static boolean authenticated = false;
    public static HashMap<ProxiedPlayer, Long> times = new HashMap<ProxiedPlayer, Long>();
    public static HashMap<ProxiedPlayer, String> uids = new HashMap<ProxiedPlayer, String>();
    public static HashMap<ProxiedPlayer, String> replies = new HashMap<ProxiedPlayer, String>();
    public static HashMap<String, String> users = new HashMap<String, String>();
    public static HashMap<String, Channel> chans = new HashMap<String, Channel>();
    Plugin plugin;

    public IRC(Socket s, Plugin p) throws IOException {
        sock = s;
        plugin = p;
        mainUid = BungeeRelay.getConfig().getString("server.id") + "AAAAAA";
        currentUid = mainUid;
        in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        out = new PrintWriter(sock.getOutputStream(), true);
        while (sock.isConnected()) handleData(in.readLine());
    }

    public void handleData(String data) throws IOException {
        if (data == null) throw new IOException();
        if (data.isEmpty()) return;
        data = data.trim();
        String[] ex = data.split(" ");

        if (BungeeRelay.getConfig().getBoolean("server.debug")) plugin.getLogger().info("Received: "+data);

        if (!authenticated && ex[0].equals("CAPAB")) {
            if (ex[1].equals("START")) {
                plugin.getLogger().info("Authenticating with server...");
                out.println("SERVER " + BungeeRelay.getConfig().getString("server.servername") + " " + BungeeRelay.getConfig().getString("server.sendpass") + " 0 " + BungeeRelay.getConfig().getString("server.id") + " :" + BungeeRelay.getConfig().getString("server.realname"));
                out.println("CAPAB START 1202");
            }
            if (ex[1].equals("END")) {
                out.println("CAPAB CAPABILITIES :NICKMAX=16 CHANMAX=64 MAXMODES=20 IDENTMAX=11 MAXQUIT=255 MAXTOPIC=307 MAXKICK=255 MAXGECOS=128 MAXAWAY=200 IP6SUPPORT=0 PROTOCOL=1202 HALFOP=0 PREFIX=" + prefixes + " CHANMODES=b,k,l,imnpst USERMODES=,,s,iow SVSPART=1");
                out.println("CAPAB END");
                out.println("BURST " + startTime);
                out.println("VERSION :0.1");
                out.println("UID " + mainUid + " " + startTime + " " + BungeeRelay.getConfig().getString("bot.nick") + " BungeeRelay " + BungeeRelay.getConfig().getString("bot.host") + " " + BungeeRelay.getConfig().getString("bot.ident") + " BungeeRelay " + startTime + " +o :" + BungeeRelay.getConfig().getString("bot.realname"));
                out.println(":" + mainUid + " OPERTYPE " + BungeeRelay.getConfig().getString("bot.opertype"));
                authenticated = true;
            }
        }

        if (ex[0].equals("SERVER") && !ex[2].equals(BungeeRelay.getConfig().getString("server.recvpass"))) {
            plugin.getLogger().warning("The server "+ex[1]+" presented the wrong password.");
            plugin.getLogger().warning("Stopping connection due to security reasons.");
            plugin.getLogger().warning("Remember that the recvpass and sendpass are opposite to the ones in your links.conf");
            out.println("ERROR :Password received was incorrect");
            sock.close();
        }

        if (ex[0].equals("CAPAB") && ex[1].equals("CAPABILITIES")) {
            if (data.contains("CHANMODES=")) chanModes = data.split("CHANMODES=")[1].split(" ")[0];
            if (data.contains("PREFIX=")) prefixes = data.split("PREFIX=")[1].split(" ")[0];
        }

        if (ex[1].equals("FJOIN")) {
            if (!chans.containsKey(ex[2])) {
                Long ts = Long.parseLong(ex[3]);
                if (!ts.equals(Util.getChanTS(ex[2]))) chans.get(ex[2]).ts = ts;
            }
            chans.get(ex[2]).users.add(ex[5].split(",")[1]);
        }

        if (ex[1].equals("ENDBURST")) {
            Util.incrementUid();
            String chan = BungeeRelay.getConfig().getString("server.channel");
            String topic = BungeeRelay.getConfig().getString("server.topic");
            String botmodes = BungeeRelay.getConfig().getString("bot.modes");
            if (chan.isEmpty()) {
                for (ServerInfo si : plugin.getProxy().getServers().values()) {
                    chan = BungeeRelay.getConfig().getString("server.chanprefix") + si.getName();
                    Util.sendMainJoin(chan, botmodes, topic.replace("{SERVERNAME}", si.getName()));
                    for (ProxiedPlayer p : si.getPlayers()) {
                        Util.sendUserConnect(p);
                        Util.sendChannelJoin(p, chan);
                        Util.incrementUid();
                    }
                }
            }else{
                Util.sendMainJoin(chan, botmodes, topic.replace("{SERVERNAME}", ""));
                for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                    Util.sendUserConnect(p);
                    Util.sendChannelJoin(p, chan);
                    Util.incrementUid();
                }
            }
            chan = BungeeRelay.getConfig().getString("server.staff");
            if (!chan.isEmpty()) {
                Util.sendMainJoin(chan, botmodes, BungeeRelay.getConfig().getString("server.stafftopic"));
                Util.giveChannelModes(chan, BungeeRelay.getConfig().getString("server.staffmodes"));
            }
            out.println("ENDBURST");
        }

        if (ex[1].equals("PING")) {
            out.println("PONG " + BungeeRelay.getConfig().getString("server.id") + " "+ex[2]);
        }

        if (ex[1].equals("UID")) {
            users.put(ex[2], ex[4]);
        }

        if (ex[1].equals("PRIVMSG")) {
            String from = users.get(ex[0].substring(1));
            String player = users.get(ex[2]);
            int prefixlen = BungeeRelay.getConfig().getString("server.userprefix").length();
            int suffixlen = BungeeRelay.getConfig().getString("server.usersuffix").length();
            Collection<ProxiedPlayer> players = new ArrayList<ProxiedPlayer>();
            boolean isPM;
            if (player != null && prefixlen < player.length() && suffixlen < player.length()) {
                ProxiedPlayer to = plugin.getProxy().getPlayer(player.substring(prefixlen, player.length() - suffixlen));
                isPM = (users.containsKey(ex[2]) && to != null);
                if (isPM) {
                    players.add(to);
                    replies.put(to, from);
                }
            }else{
                isPM = false;
            }
            if (!isPM) players = Util.getPlayersByChannel(ex[2]);
            for (ProxiedPlayer p : players) {
                int len;
                if (ex[3].equals(":" + (char) 1 + "ACTION")) {
                    len = 4;
                }else{
                    len = 3;
                }
                String s = Util.sliceStringArray(ex, len);
                String ch = Character.toString((char) 1);
                String out;
                if (len == 4) {
                    if (isPM) {
                        out = BungeeRelay.getConfig().getString("formats.privateme");
                    }else{
                        out = BungeeRelay.getConfig().getString("formats.me");
                    }
                    s = s.replaceAll(ch, "");
                }else{
                    if (isPM) {
                        out = BungeeRelay.getConfig().getString("formats.privatemsg");
                    }else{
                        out = BungeeRelay.getConfig().getString("formats.msg");
                    }
                    s = s.substring(1);
                }
                p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', out)
                        .replace("{SENDER}", from)
                        .replace("{MESSAGE}", s)));
            }
        }

        if (ex[1].equals("FMODE")) {
            String s = "";
            String d = "+";
            int v = 5;
            for (int i=0; i<ex[4].length(); i++) {
                String m = Character.toString(ex[4].charAt(i));
                String[] cm = chanModes.split(",");
                if (m.equals("b") && chans.containsKey(ex[2])) chans.get(ex[2]).bans.add(ex[v]);
                if (m.equals("+") || m.equals("-")) {
                    d = m;
                }else if (cm[0].contains(m) || cm[1].contains(m) || (cm[2].contains(m) && d.equals("+"))) {
                    s = s + ex[v] + " ";
                    v++;
                }else if (ex.length >= v && users.containsKey(ex[v])) {
                    s = s + users.get(ex[v]) + " ";
                    v++;
                }
            }
            for (ProxiedPlayer p : Util.getPlayersByChannel(ex[2])) {
                p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', BungeeRelay.getConfig().getString("formats.mode")
                        .replace("{SENDER}", users.get(ex[0].substring(1)))
                        .replace("{MODE}", ex[4] + " " + s))));
            }
        }

        if (ex[1].equals("FJOIN")) {
            for (ProxiedPlayer p : Util.getPlayersByChannel(ex[2])) {
                p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', BungeeRelay.getConfig().getString("formats.join")
                        .replace("{SENDER}", users.get(ex[5].split(",")[1])))));
            }
        }

        if (ex[1].equals("PART")) {
            String reason;
            if (ex.length > 3) {
                reason = Util.sliceStringArray(ex, 3).substring(1);
            }else{
                reason = "";
            }
            for (ProxiedPlayer p : Util.getPlayersByChannel(ex[2])) {
                p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', BungeeRelay.getConfig().getString("formats.part")
                        .replace("{SENDER}", users.get(ex[0].substring(1)))
                        .replace("{REASON}", reason))));
            }
        }

        if (ex[1].equals("KICK")) {
            String reason = Util.sliceStringArray(ex, 4).substring(1);
            String target = users.get(ex[3]);
            String sender = users.get(ex[0].substring(1));
            for (ProxiedPlayer p : Util.getPlayersByChannel(ex[2])) {
                p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', BungeeRelay.getConfig().getString("formats.kick")
                        .replace("{SENDER}", sender)
                        .replace("{TARGET}", target)
                        .replace("{REASON}", reason))));
            }
            String full = users.get(ex[3]);
            int prefixlen = BungeeRelay.getConfig().getString("server.userprefix").length();
            int suffixlen = BungeeRelay.getConfig().getString("server.usersuffix").length();
            if (BungeeRelay.getConfig().getBoolean("server.kick") && prefixlen < full.length() && suffixlen < full.length()) {
                ProxiedPlayer player = plugin.getProxy().getPlayer(full.substring(prefixlen, full.length() - suffixlen));
                if (player != null) {
                    player.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', BungeeRelay.getConfig().getString("formats.disconnectkick")
                            .replace("{SENDER}", sender)
                            .replace("{TARGET}", target)
                            .replace("{REASON}", reason))));
                }
            }
            users.remove(ex[3]);
        }

        if (ex[1].equals("QUIT")) {
            String reason;
            if (ex.length > 3) {
                reason = Util.sliceStringArray(ex, 2).substring(1);
            }else{
                reason = "";
            }

            for (Map.Entry<String, Channel> ch : chans.entrySet()) {
                String chan = BungeeRelay.getConfig().getString("server.channel");
                if (chan.isEmpty()) {
                    chan = ch.getKey();
                }else if (!ch.getKey().equals(chan)) {
                    continue;
                }
                if (!ch.getValue().users.contains(ex[0].substring(1))) continue;
                for (ProxiedPlayer p : Util.getPlayersByChannel(chan)) {
                    p.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', BungeeRelay.getConfig().getString("formats.quit")
                            .replace("{SENDER}", users.get(ex[0].substring(1)))
                            .replace("{REASON}", reason))));
                }
            }
            users.remove(ex[0].substring(1));
        }
    }
}
