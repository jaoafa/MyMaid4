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
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.PlayerVoteDataMCJP;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Event_CustomJoinMessage extends MyMaidLibrary implements Listener, EventPremise {
    static final List<String> JoinMessages = Arrays.asList(
        "the New Generation", "- Super", "Hyper", "Ultra", "Extreme", "Insane", "Gigantic", "Epic", "Amazing", "Beautiful",
        "Special", "Swag", "Lunatic", "Exotic", "God", "Hell", "Heaven", "Mega", "Giga", "Tera", "Refined", "Sharp",
        "Strong", "Muscle", "Macho", "Bomber", "Blazing", "Frozen", "Legendary", "Mystical", "Tactical", "Critical",
        "Overload", "Overclock", "Fantastic", "Criminal", "Primordial", "Genius", "Great", "Perfect", "Fearless",
        "Ruthless", "Bold", "Void", "Millenium", "Exact", "Really", "Certainty", "Infernal", "Ender", "World", "Mad",
        "Crazy", "Wrecked", "Elegant", "Expensive", "Rich", "Radioactive", "Automatic", "Honest", "Cosmic", "Galactic",
        "Dimensional", "Sinister", "Evil", "Abyssal", "Hallowed", "Holy", "Sacred", "Omnipotent"
    );

    @Override
    public String description() {
        return "独自のログインメッセージを表示するように制御します。";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent_JoinChangeMessage(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Component joinMessage = getPlayerJoinMessage(player);
        if (joinMessage != null) event.joinMessage(joinMessage);
    }

    String getJoinMessage(int count) {
        if (count < 20) {
            return null;
        } else if (count < 24) {
            return "VIP";
        } else {
            int _count = count;
            _count /= 4;
            _count -= 5;
            _count = (int) Math.floor(_count);

            return JoinMessages.stream().limit(_count).collect(Collectors.joining(" ")) + " VIP";
        }
    }

    Component getPlayerJoinMessage(Player player) {
        int count = getVoteCount(player);
        if (count < 20) {
            return null;
        }
        String rankText = getJoinMessage(count);
        String customMessage = getCustomMessage(player);
        if (customMessage != null) {
            rankText = rankText.replace("VIP", customMessage + " VIP");
        }

        return Component.text(
            String.format("%s, %s (%d) joined the game.",
                player.getName(),
                rankText,
                count),
            NamedTextColor.YELLOW);
    }

    int getVoteCount(Player player) {
        PlayerVoteDataMCJP pvd = new PlayerVoteDataMCJP(player);

        return pvd.getVoteCount();
    }

    String getCustomMessage(Player player) {
        PlayerVoteDataMCJP pvd = new PlayerVoteDataMCJP(player);

        return pvd.getCustomLoginText();
    }
}
