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

package com.jaoafa.mymaid4.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.arguments.selector.MultipleEntitySelector;
import cloud.commandframework.bukkit.parsers.selector.MultipleEntitySelectorArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.*;

import java.util.*;
import java.util.stream.Collectors;

public class Cmd_DT extends MyMaidLibrary implements CommandPremise {
    private static final Map<UUID, Long> DTCooldown = new HashMap<>();

    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "dt",
            "DynmapのMarkerを編集・テレポートします。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "マーカーにテレポートさせます。")
                .senderType(Player.class)
                .argument(StringArgument
                    .<CommandSender>newBuilder("markerName")
                    .single()
                    .withSuggestionsProvider(this::suggestMarkerNames), ArgumentDescription.of("マーカー名"))
                .handler(this::teleportMarker)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "エンティティをマーカーにテレポートさせます。")
                .literal("tp")
                .argument(MultipleEntitySelectorArgument
                    .newBuilder("targets"), ArgumentDescription.of("エンティティ対象セレクター"))
                .argument(StringArgument
                    .<CommandSender>newBuilder("markerName")
                    .withSuggestionsProvider(this::suggestMarkerNames), ArgumentDescription.of("マーカー名"))
                .handler(this::teleportOtherMarker)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "マーカーを追加します。")
                .senderType(Player.class)
                .literal("add", "set")
                .argument(StringArgument.of("markerName"), ArgumentDescription.of("マーカー名"))
                .argument(StringArgument
                    .<CommandSender>newBuilder("markerType")
                    .withSuggestionsProvider(this::suggestMarkerTypes), ArgumentDescription.of("マーカー種別"))
                .argument(StringArgument
                    .<CommandSender>newBuilder("markerIcon")
                    .asOptional()
                    .withSuggestionsProvider(this::suggestMarkerIcons), ArgumentDescription.of("マーカーアイコン"))
                .handler(this::addMarker)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "マーカーを削除します。")
                .senderType(Player.class)
                .literal("del", "remove", "rem")
                .argument(StringArgument
                    .<CommandSender>newBuilder("markerName")
                    .withSuggestionsProvider(this::suggestMarkerNames), ArgumentDescription.of("マーカー名"))
                .handler(this::delMarker)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "ランダムに選ばれたマーカーにテレポートします。")
                .senderType(Player.class)
                .literal("random")
                .handler(this::teleportRandomMarker)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "近くのマーカーを表示します。")
                .senderType(Player.class)
                .literal("near")
                .handler(this::getNearMarker)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "近くのマーカーにテレポートします。")
                .senderType(Player.class)
                .literal("neartp")
                .handler(this::getNearTpMarker)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "マーカーの一覧を表示します。")
                .senderType(Player.class)
                .literal("list")
                .argument(IntegerArgument
                    .<CommandSender>newBuilder("page")
                    .withMin(1)
                    .asOptionalWithDefault("1"), ArgumentDescription.of("ページ"))
                .handler(this::listMarkers)
                .build()
        );
    }

    void teleportMarker(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String markerName = context.get("markerName");
        if (!isAMR(player)) {
            if (DTCooldown.containsKey(player.getUniqueId()) && DTCooldown.get(player.getUniqueId()) > System.currentTimeMillis() - 3000) {
                SendMessage(context.getSender(), details(), "DTには3秒のクールダウンがあります！少々お待ちください...");
                return;
            } else {
                DTCooldown.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }

        // /dt <Marker>
        if (isDisabledPlugin("dynmap")) {
            SendMessage(player, details(), "Dynmapプラグインが動作していないため、このコマンドは使用できません。");
            return;
        }

        DynmapAPI dynmapAPI = getDynmapAPI();
        MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();

        Set<Marker> markers = markerAPI.getMarkerSets().stream()
            .flatMap(sets -> sets.getMarkers().stream())
            .collect(Collectors.toSet());

        Optional<Marker> matchedMarker = markers.stream()
            .filter(marker -> marker.getLabel().equals(markerName))
            .findFirst();

        if (matchedMarker.isEmpty()) {
            SendMessage(player, details(), "指定されたマーカー名のマーカーは見つかりませんでした。");
            Optional<Marker> perhapsMarker = markers.stream()
                .filter(marker -> marker.getLabel().equalsIgnoreCase(markerName))
                .findFirst();
            perhapsMarker.ifPresent(marker -> SendMessage(player, details(), "もしかして: " + marker.getLabel()));
            return;
        }

        teleportToMarker(player, player, matchedMarker.get());
    }

    void teleportOtherMarker(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        MultipleEntitySelector targets = context.get("targets");
        String markerName = context.get("markerName");

        // /dt tp <EntitySelector> <Marker>
        if (isDisabledPlugin("dynmap")) {
            SendMessage(sender, details(), "Dynmapプラグインが動作していないため、このコマンドは使用できません。");
            return;
        }

        if (!targets.hasAny() || targets.getEntities().isEmpty()) {
            SendMessage(context.getSender(), details(), "指定されたエンティティが見つかりませんでした。");
            return;
        }
        if (targets.getEntities().size() >= 10) {
            SendMessage(context.getSender(), details(), "指定されたエンティティが多すぎます。10以内にして下さい。");
            return;
        }

        DynmapAPI dynmapAPI = getDynmapAPI();
        MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();

        Set<Marker> markers = markerAPI.getMarkerSets().stream()
            .flatMap(sets -> sets.getMarkers().stream())
            .collect(Collectors.toSet());

        Optional<Marker> matchedMarker = markers.stream()
            .filter(marker -> marker.getLabel().equals(markerName))
            .findFirst();

        if (matchedMarker.isEmpty()) {
            SendMessage(sender, details(), "指定されたマーカー名のマーカーは見つかりませんでした。");
            Optional<Marker> perhapsMarker = markers.stream()
                .filter(marker -> marker.getLabel().equalsIgnoreCase(markerName))
                .findFirst();
            perhapsMarker.ifPresent(marker -> SendMessage(sender, details(), "もしかして: " + marker.getLabel()));
            return;
        }

        for (Entity entity : targets.getEntities()) {
            teleportToMarker(sender, entity, matchedMarker.get());
        }
    }

    void addMarker(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Location loc = player.getLocation();
        String markerName = context.get("markerName");
        String markerType = context.get("markerType");
        String markerIconId = context.getOrDefault("markerIcon", null);

        DynmapAPI dynmapAPI = getDynmapAPI();
        MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();

        MarkerSet markerSet = markerAPI.getMarkerSet(markerType);
        if (markerSet == null) {
            SendMessage(player, details(), "指定されたマーカータイプは見つかりませんでした。");
            return;
        }

        MarkerIcon markerIcon;
        if (markerIconId != null) {
            markerIcon = markerAPI.getMarkerIcon(markerIconId);
            if (markerIcon == null) {
                SendMessage(player, details(), "指定されたマーカーアイコンは見つかりませんでした。");
                return;
            }
        } else {
            markerIcon = markerSet.getDefaultMarkerIcon();
        }

        boolean isExistsEqualName = isExistsMarker(markerAPI, markerName);

        Marker marker = markerSet.createMarker(null, markerName, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), markerIcon, true);
        if (marker == null) {
            SendMessage(player, details(), "マーカーの作成に失敗しました。");
            return;
        }
        SendMessage(player, details(), String.format("マーカー「%s」を %s %.2f %.2f %.2f に作成しました。",
            marker.getLabel(), marker.getWorld(), marker.getX(), marker.getY(), marker.getZ()));

        if (isExistsEqualName) {
            SendMessage(player, details(), "同じマーカー名が他にもあるようです。期待した場所にテレポートできない可能性があります。");
        }
    }

    void delMarker(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String markerName = context.get("markerName");

        DynmapAPI dynmapAPI = getDynmapAPI();
        MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();

        Optional<Marker> _marker = markerAPI.getMarkerSets().stream()
            .flatMap(sets -> sets.getMarkers().stream())
            .filter(m -> m.getLabel().equals(markerName))
            .findFirst();

        if (_marker.isEmpty()) {
            SendMessage(player, details(), "指定されたマーカーは見つかりませんでした。");
            return;
        }
        Marker marker = _marker.get();

        SendMessage(player, details(), String.format("マーカー「%s (%s %.2f %.2f %.2f)」を削除しました。",
            marker.getLabel(), marker.getWorld(), marker.getX(), marker.getY(), marker.getZ()));

        marker.deleteMarker();
    }

    void teleportRandomMarker(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();

        DynmapAPI dynmapAPI = getDynmapAPI();
        MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();

        // ストリーム上でランダム並び替えしてひとつ取得するのが理想
        List<Marker> markers = markerAPI.getMarkerSets().stream()
            .flatMap(sets -> sets.getMarkers().stream())
            .filter(m -> m.getWorld().equals(player.getLocation().getWorld().getName()))
            .collect(Collectors.toList());
        Collections.shuffle(markers);
        if (markers.size() == 0) {
            SendMessage(player, details(), "ワールドにマーカーがひとつもありませんでした。");
            return;
        }

        teleportToMarker(player, player, markers.get(0));
    }

    void getNearMarker(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Location loc = player.getLocation();

        DynmapAPI dynmapAPI = getDynmapAPI();
        MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();

        Optional<Marker> marker = markerAPI.getMarkerSets().stream()
            .flatMap(sets -> sets.getMarkers().stream())
            .filter(m -> m.getWorld().equals(player.getLocation().getWorld().getName())).min((e1, e2) -> {
                double distance1 = new Location(Bukkit.getWorld(e1.getWorld()), e1.getX(), e1.getY(), e1.getZ()).distance(loc);
                double distance2 = new Location(Bukkit.getWorld(e2.getWorld()), e2.getX(), e2.getY(), e2.getZ()).distance(loc);
                return (int) (distance1 - distance2);
            });
        if (marker.isEmpty()) {
            SendMessage(player, details(), "ワールドにマーカーがひとつもありませんでした。");
            return;
        }
        double distance = player.getLocation().distance(new Location(Bukkit.getWorld(marker.get().getWorld()), marker.get().getX(), marker.get().getY(), marker.get().getZ()));

        SendMessage(player, details(), String.format("あなたから一番近い場所にあるマーカーは約%.3fブロック程度のところにある「%s」というマーカーです。", distance, marker.get().getLabel()));
        SendMessage(player, details(), Component.text().append(
            Component.text("/dt neartp", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                .hoverEvent(HoverEvent.showText(Component.text("/dt neartp を実行します。")))
                .clickEvent(ClickEvent.runCommand("/dt neartp")),
            Component.text(" でテレポートできます。", NamedTextColor.GREEN)
        ).build());
    }

    void getNearTpMarker(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Location loc = player.getLocation();

        DynmapAPI dynmapAPI = getDynmapAPI();
        MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();

        Optional<Marker> marker = markerAPI.getMarkerSets().stream()
            .flatMap(sets -> sets.getMarkers().stream())
            .filter(m -> m.getWorld().equals(player.getLocation().getWorld().getName())).min((e1, e2) -> {
                double distance1 = new Location(Bukkit.getWorld(e1.getWorld()), e1.getX(), e1.getY(), e1.getZ()).distance(loc);
                double distance2 = new Location(Bukkit.getWorld(e2.getWorld()), e2.getX(), e2.getY(), e2.getZ()).distance(loc);
                return (int) (distance1 - distance2);
            });
        if (marker.isEmpty()) {
            SendMessage(player, details(), "ワールドにマーカーがひとつもありませんでした。");
            return;
        }

        teleportToMarker(player, player, marker.get());
    }

    void listMarkers(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        int page = context.<Integer>get("page");

        DynmapAPI dynmapAPI = getDynmapAPI();
        MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();

        List<Marker> markers = markerAPI.getMarkerSets().stream()
            .flatMap(sets -> sets.getMarkers().stream()).toList();

        int pageItemNum = 5;
        int skipNum = (page - 1) * pageItemNum;

        Component header = Component.text().append(
            Component.text("----- "),
            Component.text("DYNMAP MARKERS", NamedTextColor.GOLD),
            Component.text(" -----")
        ).build();
        SendMessage(player, details(), header);

        markers.stream().skip(skipNum).limit(pageItemNum).forEach(m -> {
            Component componentHomeInfo = Component.text().append(
                Component.text(formatText(m.getLabel(), 7), NamedTextColor.GOLD)
                    .hoverEvent(HoverEvent.showText(Component.text(m.getLabel())))
                    .clickEvent(ClickEvent.runCommand(String.format("/home %s", m.getLabel()))),
                Component.text(formatText(m.getWorld(), 14), NamedTextColor.AQUA)
                    .hoverEvent(HoverEvent.showText(Component.text(m.getWorld()))),
                Component.space(),
                Component.text(formatText(String.format("%.1f", m.getX()), 5))
                    .hoverEvent(HoverEvent.showText(Component.text(m.getX()))),
                Component.space(),
                Component.text(formatText(String.format("%.1f", m.getY()), 5))
                    .hoverEvent(HoverEvent.showText(Component.text(m.getY()))),
                Component.space(),
                Component.text(formatText(String.format("%.1f", m.getZ()), 5))
                    .hoverEvent(HoverEvent.showText(Component.text(m.getZ())))
            ).build();
            SendMessage(player, details(), componentHomeInfo);
        });
        Component footer = Component.text().append(
            Component.text("---<< ")
                .clickEvent(ClickEvent.runCommand(String.format("/dt list %d", page - 1))),
            Component.text("[" + (page - 1) + "] PAGE", NamedTextColor.GOLD),
            Component.text(" >>---")
                .clickEvent(ClickEvent.runCommand(String.format("/dt list %d", page + 1)))
        ).build();
        SendMessage(player, details(), footer);
    }

    String formatText(String str, int width) {
        return width == str.length() ? str :
            width > str.length() ? str + StringUtils.repeat(" ", width - str.length()) :
                str.substring(0, width);
    }

    /**
     * マーカー名のリストを返す
     *
     * @param context CommandContext
     * @param current 入力されている値
     *
     * @return サジェストする文字列一覧
     */
    List<String> suggestMarkerNames(final CommandContext<CommandSender> context, final String current) {
        if (isDisabledPlugin("dynmap")) {
            return new ArrayList<>();
        }
        DynmapAPI dynmapAPI = getDynmapAPI();
        MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();

        return markerAPI.getMarkerSets().stream()
            .flatMap(a -> a.getMarkers().stream()).toList().stream()
            .map(GenericMarker::getLabel)
            .filter(label -> label.startsWith(current))
            .collect(Collectors.toList());
    }

    /**
     * マーカータイプのリストを返す
     *
     * @param context CommandContext
     * @param current 入力されている値
     *
     * @return サジェストする文字列一覧
     */
    List<String> suggestMarkerTypes(final CommandContext<CommandSender> context, final String current) {
        if (isDisabledPlugin("dynmap")) {
            return new ArrayList<>();
        }
        DynmapAPI dynmapAPI = getDynmapAPI();
        MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();

        return markerAPI.getMarkerSets().stream()
            .map(MarkerSet::getMarkerSetID)
            .filter(label -> label.startsWith(current))
            .collect(Collectors.toList());
    }

    /**
     * マーカーアイコンのリストを返す
     *
     * @param context CommandContext
     * @param current 入力されている値
     *
     * @return サジェストする文字列一覧
     */
    List<String> suggestMarkerIcons(final CommandContext<CommandSender> context, final String current) {
        if (isDisabledPlugin("dynmap")) {
            return new ArrayList<>();
        }
        DynmapAPI dynmapAPI = getDynmapAPI();
        MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();

        return markerAPI.getMarkerIcons().stream()
            .map(MarkerIcon::getMarkerIconID)
            .filter(label -> label.startsWith(current))
            .collect(Collectors.toList());
    }

    /**
     * DynmapAPIを返す
     *
     * @return DynmapAPI
     */
    DynmapAPI getDynmapAPI() {
        Plugin dynmap = Bukkit.getPluginManager().getPlugin("dynmap");
        return (DynmapAPI) dynmap;
    }

    /**
     * マーカーが存在するかどうかを返す
     *
     * @param markerAPI  マーカーAPI
     * @param markerName 調べるマーカー名
     *
     * @return 存在すればTrue
     */
    boolean isExistsMarker(MarkerAPI markerAPI, String markerName) {
        return markerAPI.getMarkerSets().stream()
            .flatMap(a -> a.getMarkers().stream())
            .anyMatch(marker -> marker.getLabel().equals(markerName));
    }

    void teleportToMarker(CommandSender sender, Entity entity, Marker marker) {
        String markerName = marker.getLabel();
        World world = Bukkit.getWorld(marker.getWorld());
        if (world == null) {
            SendMessage(sender, details(), String.format("指定されたマーカーのワールド「%s」が見つかりませんでした。", marker.getWorld()));
            return;
        }
        Location loc = new Location(world, marker.getX(), marker.getY(), marker.getZ());
        if (loc.getX() % 1 == 0 && loc.getZ() % 1 == 0) {
            // 小数点以下が指定されていない場合に0.5を足す
            loc.add(0.5f, 0f, 0.5f);
        }
        entity.teleport(loc);

        // 可読性悪すぎ
        HoverEvent<?> senderHoverEvent =
            sender == entity ?
                HoverEvent.showEntity(
                    Key.key("player"),
                    entity.getUniqueId(),
                    Component.text(entity.getName())) :
                sender instanceof Player ?
                    HoverEvent.showEntity(
                        Key.key("player"),
                        ((Player) sender).getUniqueId(),
                        Component.text(sender.getName())) :
                    HoverEvent.showText(Component.text(sender.getName()));

        Bukkit.getServer().sendMessage(Component.text().append(
            Component.text("[", NamedTextColor.GRAY),
            Component.text(sender.getName(), NamedTextColor.GRAY, TextDecoration.ITALIC)
                .hoverEvent(senderHoverEvent),
            Component.text(":", NamedTextColor.GRAY),
            Component.space(),
            Component.text(entity.getName(), NamedTextColor.GRAY, TextDecoration.ITALIC)
                .hoverEvent(HoverEvent.showEntity(
                    entity.getType().getKey(),
                    entity.getUniqueId(),
                    Component.text(entity.getName()))),
            Component.space(),
            Component.text("を", NamedTextColor.GRAY),
            Component.space(),
            Component.text(markerName, NamedTextColor.GRAY, TextDecoration.ITALIC)
                .hoverEvent(HoverEvent.showText(Component.text().append(
                    Component.text("Dynmap Marker"),
                    Component.newline(),
                    Component.text(world.getName()),
                    Component.space(),
                    Component.text(loc.getX()),
                    Component.space(),
                    Component.text(loc.getY()),
                    Component.space(),
                    Component.text(loc.getZ())
                )))
                .clickEvent(ClickEvent.suggestCommand(String.format("/dt %s", markerName))),
            Component.space(),
            Component.text("にテレポートさせました]", NamedTextColor.GRAY)
        ));
        if (MyMaidData.getServerChatChannel() != null) {
            MyMaidData.getServerChatChannel().sendMessage("*[" + DiscordEscape(sender.getName()) + ": " + DiscordEscape(entity.getName()) + " teleported to " + markerName + "]*").queue();
        }
    }
}
