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

import com.jaoafa.mymaid4.customEvents.TeleportCommandEvent;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.TpDeny;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Event_TpDeny extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "tpdenyコマンドに関する処理を行います。";
    }

    @EventHandler
    public void onTeleportCommand(TeleportCommandEvent event) {
        // 以下を禁止する (A: BをtpDenyしている | B: AからtpDenyされている | C: 無関係、コマブロなどの場合有)
        // B: B -> A (禁止されている側からテレポートできない)
        // B: C -> A (禁止されている側からターゲットとしてテレポート操作できない)
        // B: A -> C
        // B: A -> B
        // C: B -> A
        // C: A -> B

        // getSender: FromPlayer -> ToPlayer
        if (event.getSender() instanceof Player) {
            // 実行者がプレイヤーの場合
            senderIsPlayer(event);
        } else {
            // 実行者がプレイヤー以外の場合
            senderIsNonPlayer(event);
        }
    }

    void senderIsPlayer(TeleportCommandEvent event) {
        CommandSender sender = event.getSender();
        // ターゲットが実行者を拒否している場合
        // From
        TpDeny fromTpDeny = new TpDeny(event.getFromPlayer());
        if (fromTpDeny.isTpDeny((Player) sender)) {
            sender.sendMessage(Component.text().append(
                Component.text("[tpDeny] "),
                Component.text("指定されたプレイヤー「" + sender.getName() + "」のテレポート操作は拒否されました。", NamedTextColor.GREEN)
            ));
            if (fromTpDeny.isNotify((Player) sender)) {
                sender.sendMessage(Component.text().append(
                    Component.text("[tpDeny] "),
                    Component.text("プレイヤー「" + sender.getName() + "」のテレポート操作を拒否しました。", NamedTextColor.GREEN)
                ));
            }
            event.setCancelled(true);
            return;
        }

        // ターゲットがテレポート者を拒否している場合
        if (event.getToPlayer() != null) {
            TpDeny toTpDeny = new TpDeny(event.getToPlayer());
            if (toTpDeny.isTpDeny(event.getFromPlayer())) {
                sender.sendMessage(Component.text().append(
                    Component.text("[tpDeny] "),
                    Component.text("指定されたプレイヤー「" + sender.getName() + "」へのテレポートは拒否されました。", NamedTextColor.GREEN)
                ));
                if (toTpDeny.isNotify((Player) sender)) {
                    sender.sendMessage(Component.text().append(
                        Component.text("[tpDeny] "),
                        Component.text("プレイヤー「" + event.getFromPlayer().getName() + "」からのテレポートを拒否しました。", NamedTextColor.GREEN)
                    ));
                }
                event.setCancelled(true);
            }
        }
    }

    void senderIsNonPlayer(TeleportCommandEvent event) {
        CommandSender sender = event.getSender();
        if (event.getToPlayer() == null) {
            return;
        }
        TpDeny tpdeny = new TpDeny(event.getToPlayer());
        if (!tpdeny.isTpDeny(event.getFromPlayer())) {
            return;
        }
        sender.sendMessage(Component.text().append(
            Component.text("[tpDeny] "),
            Component.text("指定されたプレイヤー「" + event.getToPlayer().getName() + "」のテレポート操作は拒否されました。", NamedTextColor.GREEN)
        ));
        event.setCancelled(true);
    }
}
