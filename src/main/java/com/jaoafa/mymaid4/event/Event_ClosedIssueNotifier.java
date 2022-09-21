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

import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.IssueManager;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;
import java.util.List;

public class Event_ClosedIssueNotifier extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "ログイン時、bugコマンドやfeedbackコマンドによって作成されたissueがクローズされていたら通知します。";
    }

    @EventHandler
    public void onJoinClearCache(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            public void run() {
                List<IssueManager.Issue> issues = IssueManager.getPlayerIssues(player);
                if (issues.isEmpty()) {
                    return;
                }
                List<IssueManager.Issue> closedIssues = issues.stream()
                    .filter(issue -> IssueManager.isClosedIssue(issue.repository(), issue.issueNumber()))
                    .toList();
                if (closedIssues.isEmpty()) {
                    return;
                }
                player.sendMessage(Component.text().append(
                    Component.text("["),
                    Component.text("BugFeedback", NamedTextColor.YELLOW),
                    Component.text("]"),
                    Component.space(),
                    Component.text("あなたが送信した不具合報告・フィードバックが完了済みとしてマークされました。", NamedTextColor.GREEN)
                ));

                for (IssueManager.Issue issue : closedIssues) {
                    String datetime = sdfFormat(new Date(issue.time()));
                    player.sendMessage(Component.text().append(
                        Component.text("["),
                        Component.text("BugFeedback", NamedTextColor.YELLOW),
                        Component.text("]"),
                        Component.space(),
                        Component.text(datetime + " に送信された", NamedTextColor.GREEN),
                        Component.text(IssueManager.getRepoIssueType(issue.repository()), NamedTextColor.GREEN),
                        Component.text(":", NamedTextColor.GREEN),
                        Component.space(),
                        Component.text("%s#%d".formatted(issue.repository().getRepo(), issue.issueNumber()), NamedTextColor.AQUA)
                            .hoverEvent(HoverEvent.showText(Component.text("クリックで不具合報告・フィードバックのページを開きます", NamedTextColor.GREEN)))
                            .clickEvent(ClickEvent.openUrl("https://github.com/%s/issues/%d".formatted(issue.repository().getRepo(), issue.issueNumber())))
                    ));
                    IssueManager.removePlayerIssue(player, issue.issueNumber());
                }

            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }
}