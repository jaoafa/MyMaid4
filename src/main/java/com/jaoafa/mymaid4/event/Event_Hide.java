package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.MyMaidConfig;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import static com.jaoafa.mymaid4.lib.MyMaidLibrary.isAM;

public class Event_Hide implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onAsyncPlayerChatEvent(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Component msg = event.message();
        if (!MyMaidConfig.isHid(player.getUniqueId())) {
            return;
        }
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (!isAM(p)) {
                continue;
            }
            Component component = Component.text().append(
                Component.text(ChatColor.GRAY + player.getName() + " > " + msg)
            ).build();
            p.sendMessage(component);
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }
            if (!MyMaidConfig.isHid(p.getUniqueId())) {
                player.showPlayer(Main.getJavaPlugin(), p);
                continue;
            }
            player.hidePlayer(Main.getJavaPlugin(), p);

            p.sendMessage("[Hide] " + ChatColor.RED + "プレイヤー「" + player.getName() + "」にあなたのhideモードを反映しました。");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (!MyMaidConfig.isHid(p.getUniqueId())) {
                continue;
            }
            if (!command.toLowerCase().contains(p.getName().toLowerCase())) {
                continue;
            }
            player.sendMessage("Entity '%s' cannot be found".replace("%s", p.getName()));
            event.setCancelled(true);
            return;
        }
    }
}
