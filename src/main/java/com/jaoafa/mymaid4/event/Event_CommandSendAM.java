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

import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Event_CommandSendAM extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "実行されたコマンドをAdminとModeratorに通知します。";
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();
        if (isAMRV(player)) {
            // Default以上は実行試行したコマンドを返す
            player.sendMessage(ChatColor.DARK_GRAY + "Cmd: " + command); // 仮
        }
        String group = getPermissionMainGroup(player);
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (isAM(p) && (!player.getName().equals(p.getName()))) {
                p.sendMessage(
                    ChatColor.GRAY + "(" + group + ") " + player.getName() + ": " + ChatColor.YELLOW + command + (event.isCancelled() ? ChatColor.RED + " (Canceled)" : ""));
            }
        }

        // Lunachat - jp translate

        // TODO 実装する
        /*
        if (!Bukkit.getServer().getPluginManager().isPluginEnabled("LunaChat")) {
            return;
        }
        LunaChat lunachat = (LunaChat) Bukkit.getServer().getPluginManager().getPlugin("LunaChat");
        LunaChatAPI lunachatapi = lunachat.getLunaChatAPI();

        if (!command.contains(" ")) {
            return;
        }
        String[] commands = command.split(" ", 0);
        List<String> tells = new ArrayList<String>() {
            {
                add("/tell");
                add("/msg");
                add("/message");
                add("/m");
                add("/t");
                add("/w");
            }
        };

        if (tells.contains(commands[0])) {
            if (commands.length <= 2) {
                return;
            }
            String text = String.join(" ", Arrays.copyOfRange(commands, 2, commands.length));
            if (!lunachatapi.isPlayerJapanize(player.getName())) {
                return;
            }
            String jp = lunachatapi.japanize(text, JapanizeType.GOOGLE_IME);
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                if (isAM(p) && (!player.getName().equals(p.getName()))) {
                    p.sendMessage(ChatColor.GRAY + "(" + ChatColor.YELLOW + jp + ChatColor.GRAY + ")");
                }
            }
        } else if (commands[0].equalsIgnoreCase("/r")) {
            if (commands.length <= 1) {
                return;
            }
            String text = String.join(" ", Arrays.copyOfRange(commands, 1, commands.length));
            if (!lunachatapi.isPlayerJapanize(player.getName())) {
                return;
            }
            String jp = lunachatapi.japanize(text, JapanizeType.GOOGLE_IME);
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                if (isAM(p) && (!player.getName().equals(p.getName()))) {
                    p.sendMessage(ChatColor.GRAY + "(" + ChatColor.YELLOW + jp + ChatColor.GRAY + ")");
                }
            }
        }
        */
    }
}