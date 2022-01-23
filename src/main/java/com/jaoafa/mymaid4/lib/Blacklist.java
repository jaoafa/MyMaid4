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

package com.jaoafa.mymaid4.lib;

import com.jaoafa.mymaid4.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Blacklist {
    static final Map<BlacklistItem, Long> notifySendTime = new HashMap<>();
    final Set<BlacklistItem> items;

    public Blacklist() {
        items = new HashSet<>();
        JavaPlugin plugin = Main.getJavaPlugin();
        File file = new File(plugin.getDataFolder(), "blacklist.json");
        if (!file.exists()) {
            plugin.getLogger().warning("blacklist.json が見つかりません。ブラックリスト機能は動作しません。");
            return;
        }

        try {
            String body = String.join("\n", Files.readAllLines(file.toPath()));
            JSONObject object = new JSONObject(body);
            for (String blockName : object.keySet()) {
                JSONObject settings = object.getJSONObject(blockName);
                Set<Material> materials = getMatchMaterials(blockName);

                for (Material material : materials) {
                    items.add(new BlacklistItem(
                        material,
                        settings.has("ignore-groups") ? toStringList(settings.getJSONArray("ignore-groups").toList()) : null,
                        settings.has("ignore-perms") ? settings.getString("ignore-perms") : null,
                        settings.has("on-break") ? getMatchActions(toStringList(settings.getJSONArray("on-break").toList())) : null,
                        settings.has("on-place") ? getMatchActions(toStringList(settings.getJSONArray("on-place").toList())) : null,
                        settings.has("on-use") ? getMatchActions(toStringList(settings.getJSONArray("on-use").toList())) : null,
                        settings.has("on-interact") ? getMatchActions(toStringList(settings.getJSONArray("on-interact").toList())) : null,
                        settings.has("on-drop") ? getMatchActions(toStringList(settings.getJSONArray("on-drop").toList())) : null,
                        settings.has("on-acquire") ? getMatchActions(toStringList(settings.getJSONArray("on-acquire").toList())) : null,
                        settings.has("on-equip") ? getMatchActions(toStringList(settings.getJSONArray("on-equip").toList())) : null
                    ));
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("blacklist.json を読み込むときに IOException が発生しました。");
            MyMaidLibrary.reportError(getClass(), e);
        } catch (JSONException e) {
            plugin.getLogger().warning("blacklist.json を読み込むときに JSONException が発生しました。");
            MyMaidLibrary.reportError(getClass(), e);
        }
    }

    public Set<BlacklistItem> getItems() {
        return items;
    }


    Set<Material> getMatchMaterials(String name) {
        Pattern p = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
        return Arrays.stream(Material.values())
            .filter(s -> p.matcher(s.name()).matches())
            .collect(Collectors.toSet());
    }

    BlacklistAction[] getMatchActions(List<String> list) {
        BlacklistAction[] actions = Arrays.stream(BlacklistAction.values())
            .filter(a -> list.stream().map(String::toUpperCase).toList().contains(a.name()))
            .toArray(BlacklistAction[]::new);
        return actions.length != 0 ? actions : null;
    }

    List<String> toStringList(List<Object> objectList) {
        return objectList.stream()
            .map(o -> Objects.toString(o, null))
            .collect(Collectors.toList());
    }

    public enum BlacklistEvent {
        BREAK("破壊"),
        PLACE("設置"),
        USE("使用"),
        INTERACT("インタラクト"),
        DROP("ドロップ"),
        EQUIP("装備"),
        ACQUIRE("拾得");

        final String name;

        BlacklistEvent(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum BlacklistAction {
        DENY(context -> false),
        NOTIFY(context -> {
            if (notifySendTime.containsKey(context.getBlacklistItem())) {
                long time = notifySendTime.get(context.getBlacklistItem());
                if (time >= System.currentTimeMillis() - 30 * 1000) {
                    return true; // 30 sec
                }
            }
            notifySendTime.put(context.getBlacklistItem(), System.currentTimeMillis());

            Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("worldguard.notify"))
                .forEach(p -> p.sendMessage(Component.text().append(
                    Component.text("[BLACKLIST] ", NamedTextColor.RED),
                    Component.text(MessageFormat.format("{0} が {1} を {2} で{3}しました",
                        context.getPlayer().getName(),
                        context.getBlacklistItem().getMaterial().name(),
                        MyMaidLibrary.formatLocation(context.getLocation()),
                        context.getBlacklistEvent().getName()), NamedTextColor.GRAY)
                )));
            return true;
        }),
        LOG(context -> {
            Main.getJavaPlugin().getLogger()
                .info("[BLACKLIST] " + MessageFormat.format("{0} が {1} を {2} で{3}しました",
                    context.getPlayer().getName(),
                    context.getBlacklistItem().getMaterial().name(),
                    MyMaidLibrary.formatLocation(context.getLocation()),
                    context.getBlacklistEvent().getName()));
            return true;
        }),
        TELL(context -> {
            context.getPlayer().sendMessage(Component.text().append(
                Component.text("[BLACKLIST] "),
                Component.text(MessageFormat.format("あなたは{0}を{1}することはできません。",
                    context.getBlacklistItem().getMaterial().name(),
                    context.getBlacklistEvent().getName()), NamedTextColor.RED)
            ));
            return true;
        }),
        KICK(context -> {
            context.getPlayer().kick(Component.text().append(
                Component.text("[BLACKLIST] "),
                Component.text(MessageFormat.format("あなたは{0}を{1}することはできません。",
                    context.getBlacklistItem().getMaterial().name(),
                    context.getBlacklistEvent().getName()), NamedTextColor.RED)
            ).build());
            return true;
        }),
        BAN(context -> {
            context.getPlayer().banPlayer(MessageFormat.format("[BLACKLIST] あなたは{0}を{1}することはできません。",
                context.getBlacklistItem().getMaterial().name(),
                context.getBlacklistEvent().getName()));
            return true;
        });

        private final ActionHandler handler;

        BlacklistAction(ActionHandler handler) {
            this.handler = handler;
        }

        public ActionHandler getHandler() {
            return handler;
        }

        @FunctionalInterface
        public interface ActionHandler {
            boolean execute(@NotNull BlacklistContext context);
        }

        public record BlacklistContext(BlacklistItem blacklistItem,
                                       BlacklistEvent blacklistEvent,
                                       Player player, Location location) {

            public BlacklistItem getBlacklistItem() {
                return blacklistItem;
            }

            public BlacklistEvent getBlacklistEvent() {
                return blacklistEvent;
            }

            public Player getPlayer() {
                return player;
            }

            public Location getLocation() {
                return location;
            }
        }
    }

    public static class BlacklistItem {
        private final Material material;
        private final List<String> ignoreGroups;
        private final String ignorePerms;
        private final Map<BlacklistEvent, BlacklistAction[]> events = new HashMap<>();

        BlacklistItem(Material material, List<String> ignoreGroups, String ignorePerms, BlacklistAction[] onBreak, BlacklistAction[] onPlace, BlacklistAction[] onUse, BlacklistAction[] onInteract, BlacklistAction[] onDrop, BlacklistAction[] onAcquire, BlacklistAction[] onEquip) {
            this.material = material;
            this.ignoreGroups = ignoreGroups;
            this.ignorePerms = ignorePerms;
            if (onBreak != null) this.events.put(BlacklistEvent.BREAK, onBreak);
            if (onPlace != null) this.events.put(BlacklistEvent.PLACE, onPlace);
            if (onUse != null) this.events.put(BlacklistEvent.USE, onUse);
            if (onInteract != null) this.events.put(BlacklistEvent.INTERACT, onInteract);
            if (onDrop != null) this.events.put(BlacklistEvent.DROP, onDrop);
            if (onAcquire != null) this.events.put(BlacklistEvent.ACQUIRE, onAcquire);
            if (onEquip != null) this.events.put(BlacklistEvent.EQUIP, onEquip);
        }

        public Material getMaterial() {
            return material;
        }

        public List<String> getIgnoreGroups() {
            return ignoreGroups;
        }

        public String getIgnorePerms() {
            return ignorePerms;
        }

        public BlacklistAction[] getEventActions(BlacklistEvent event) {
            return events.get(event);
        }

        public boolean isEventActive(BlacklistEvent event) {
            return events.containsKey(event);
        }

        @Override
        public String toString() {
            return "BlacklistItem{" +
                "material=" + material +
                ", ignoreGroups=" + ignoreGroups +
                ", ignorePerms='" + ignorePerms + '\'' +
                ", events=" + events +
                '}';
        }
    }
}
