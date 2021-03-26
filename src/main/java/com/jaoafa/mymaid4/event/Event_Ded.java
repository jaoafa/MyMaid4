/*
 * jaoLicense
 *
 * Copyright (c) 2021 jao Minecraft Server
 *
 * The following license applies to this project: jaoLicense
 *
 * Japanese: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE.md
 * English: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE-en.md
 */

package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class Event_Ded extends MyMaidLibrary implements Listener {
    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location loc = player.getLocation();
        MyMaidData.setLastDed(player.getName(), loc);
        player.sendMessage("[DED] " + ChatColor.GREEN + "死亡した場所に戻るには「/ded」コマンドが使用できます。");
        player.sendMessage("[DED] " + ChatColor.RED + "" + ChatColor.BOLD + "=== [!] 警告 ===");
        player.sendMessage("[DED] " + ChatColor.RED + "" + ChatColor.BOLD + "PvP等での「/ded」コマンドの利用は原則禁止です。");
        player.sendMessage("[DED] " + ChatColor.RED + "" + ChatColor.BOLD + "多く使用すると迷惑行為として認識される場合もあります！");
    }
}
