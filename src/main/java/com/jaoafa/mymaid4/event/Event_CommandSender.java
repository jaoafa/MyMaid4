/*
 * jaoLicense
 *
 * Copyright (c) 2022 jao Minecraft Server
 *
 * The following license applies to this project: jaoLicense
 *
 * Japanese: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE.md
 * English: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE-en.md
 */

package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Event_CommandSender extends MyMaidLibrary implements Listener, EventPremise {
    private static void sendCmd(Player player, Player executer, String group, String command, PlayerCommandPreprocessEvent event) {
        player.sendMessage(
            Component.text()
                .color(NamedTextColor.DARK_GRAY)
                .append(
                    // [Group/PlayerName] command test test (取り消し済み)
                    Component.text(
                        String.format("[%s|", group),
                        NamedTextColor.GRAY
                    ),
                    Component.text(
                        executer.getName(),
                        Style.style()
                            .color(NamedTextColor.GRAY)
                            .decorate(TextDecoration.UNDERLINED)
                            .clickEvent(ClickEvent.runCommand("/secrettp " + executer.getName()))
                            .hoverEvent(HoverEvent.showText(
                                Component.text(String.format("スペクテイターで%sにテレポート", executer.getName()))
                            ))
                            .build()
                    ),
                    Component.text(
                        "] ",
                        NamedTextColor.GRAY
                    ),
                    Component.text(command, NamedTextColor.YELLOW),
                    Component.text((event.isCancelled() ? " (拒否)" : ""), NamedTextColor.RED)
                )
        );

    }

    @Override
    public String description() {
        return "実行されたコマンドを特定権限に通知します。";
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player executor = event.getPlayer();
        String command = event.getMessage();
        String group = getPermissionMainGroup(executor);

        //feedback
        if (isAMRV(executor)) {
            // Verified以上は実行試行したコマンドを返す
            executor.sendMessage(
                Component.text()
                    .color(NamedTextColor.DARK_GRAY)
                    .append(
                        Component.text("["),
                        Component.text("Cmd", Style.style(TextDecoration.UNDERLINED, ClickEvent.copyToClipboard(command)).toBuilder().build()),
                        Component.text("] " + command)
                    )
            );
        }

        //sender
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            //TempMute or 実行者本人
            if (MyMaidData.getTempMuting().contains(executor) || executor.getName().equals(player.getName())) {
                continue;
            }

            //送り先がVD
            if (isV(player) || isD(player)) {
                continue;
            }

            //送り先AM & 実行者AMRVD -> 送る
            if (isAM(player)) {
                sendCmd(player, executor, group, command, event);
            }

            //送り先がR & 実行者がRVD -> 送る
            if (isR(player) && (isV(executor) || isD(executor))) {
                sendCmd(player, executor, group, command, event);
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