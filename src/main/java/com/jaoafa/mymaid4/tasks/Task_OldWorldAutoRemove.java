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

package com.jaoafa.mymaid4.tasks;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class Task_OldWorldAutoRemove extends BukkitRunnable {
    List<String> targetWorlds = List.of(
        "kassi-hp-tk",
        "Jao_Afa_1",
        "Jao_Afa_2",
        "Jao_Afa_3",
        "Jao_Afa_nether_1",
        "Jao_Afa_nether_2",
        "SandBox_1",
        "SandBox_2",
        "SandBox_3",
        "jaoTest1.18.1",
        "ReJao_Afa",
        "Summer2017",
        "Summer2018",
        "Summer2020"
    );

    List<String> nextRemoveWorlds = new ArrayList<>();

    @Override
    public void run() {
        Bukkit.getWorlds().stream().filter(world -> targetWorlds.contains(world.getName())).forEach(world -> {
            if (world.getPlayers().size() != 0) {
                nextRemoveWorlds.remove(world.getName());
                return;
            }
            if (!nextRemoveWorlds.contains(world.getName())) {
                sendMessageOP(Component.text("2分後にワールド「%s」にプレイヤーがいなければ自動的にアンロードされます。".formatted(world.getName())));
                nextRemoveWorlds.add(world.getName());
                return;
            }
            sendMessageOP(Component.text("ワールド「%s」をアンロードします。".formatted(world.getName())));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv remove %s".formatted(world.getName()));
        });
    }

    void sendMessageOP(Component component) {
        Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp).forEach(player -> player.sendMessage(Component.join(
            JoinConfiguration.noSeparators(),
            Component.text("[OldWorldAutoRemove]"),
            Component.space(),
            component.colorIfAbsent(NamedTextColor.GREEN)
        )));
    }
}
