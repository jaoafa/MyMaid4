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
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONException;
import org.json.JSONObject;
import us.myles.ViaVersion.api.Via;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Event_ViaVerCheck extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "サーババージョン以外でのバージョンでログインした際に通知を表示します。";
    }

    @EventHandler
    public void OnPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            public void run() {
                if (!player.isOnline()) {
                    return;
                }
                if (Bukkit.getPluginManager().getPlugin("ViaVersion") == null) {
                    return;
                }
                int i = Via.getAPI().getPlayerVersion(player.getUniqueId());
                String ver = String.valueOf(i);

                // https://wiki.vg/Protocol_version_numbers
                InputStream is = getClass().getResourceAsStream("/versions.json");
                if (is == null) {
                    return;
                }
                JSONObject obj;
                try {
                    String str = toString(is);
                    obj = new JSONObject(str);
                } catch (IOException | JSONException e) {
                    MyMaidLibrary.reportError(getClass(), e);
                    return;
                }

                if (!obj.has(ver)) {
                    // version not found.
                    sendAMR(Component.text().append(
                        Component.text("[ViaVersion]"),
                        Component.space(),
                        Component.text(String.format("%s -> (%s)", player.getName(), ver), NamedTextColor.AQUA)
                    ).build());
                    return;
                }

                String version = obj.getString(ver);

                if (!version.equals("1.18.2")) {
                    player.sendMessage(Component.text().append(
                        Component.text("[VersionChecker]"),
                        Component.space(),
                        Component.text("あなたはクライアントバージョン「" + version + "」で接続しています。", NamedTextColor.AQUA)
                    ).build());
                    player.sendMessage(Component.text().append(
                        Component.text("[VersionChecker]"),
                        Component.space(),
                        Component.text("サーババージョンは1.18.2のため、", NamedTextColor.AQUA),
                        Component.text("一部のブロック・機能は利用できません。", NamedTextColor.RED)
                    ).build());
                    player.sendMessage(Component.text().append(
                        Component.text("[VersionChecker]"),
                        Component.space(),
                        Component.text("クライアントバージョンを「1.18.2」にすることを強くお勧めします。", NamedTextColor.AQUA)
                    ).build());
                }

                sendAMR(Component.text().append(
                    Component.text("[ViaVersion]"),
                    Component.space(),
                    Component.text(String.format("%s -> %s (%s)", player.getName(), version, ver), NamedTextColor.AQUA)
                ).build());
            }

            String toString(InputStream is) throws IOException {
                InputStreamReader reader = new InputStreamReader(is);
                StringBuilder builder = new StringBuilder();
                char[] buf = new char[1024];
                int numRead;
                while (0 <= (numRead = reader.read(buf))) {
                    builder.append(buf, 0, numRead);
                }
                reader.close();
                return builder.toString();
            }
        }.runTaskLater(Main.getJavaPlugin(), 10L);
    }
}
