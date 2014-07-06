BungeeRelay
==========

BungeeRelay is a proxy plugin for BungeeCord that aims to take IRC to a whole new level, by providing seamless integration with your IRC network. The majority of pre-existing solutions consist of a single bot connecting to a network and relaying any chat information. This plugin takes this a step further by utilising InspIRCd's powerful linking protocol, meaning that each player appears as a separate user from IRC. This opens up many possibilities, as you can interact with players as normal IRC users, rather than doing it through bot commands. It also means that your IRC channel user list will be populated with your players, rather than a single relay bot.

For further information, please read the project's page in [the Spigot resource section](http://www.spigotmc.org/resources/bungeerelay.325/)

Setting up
----------
To use this plugin on your own server, you simply need to drop this plugin and the BungeeYAML plugin into your BungeeCord installation's "plugins" folder. When you first run the plugin, a default config.yml will be created there, which contains some tips on configuring the plugin.

Compiling
---------
The plugin makes use of Maven to enable fast and easy compiling. If you wish to compile the plugin, simply run `mvn package`.

Licence
-------
This plugin and its source code is licenced under the [Creative Commons Attribution-ShareAlike 4.0 International licence](http://creativecommons.org/licenses/by-sa/4.0/deed.en_US). The "Attribution" section of the licence is attained by leaving the credit already in BungeeRelay, namely in the plugin.yml file.