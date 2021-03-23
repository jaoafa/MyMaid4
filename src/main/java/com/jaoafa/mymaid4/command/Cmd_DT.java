package com.jaoafa.mymaid4.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Cmd_DT extends MyMaidLibrary implements CommandPremise {
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
                .meta(CommandMeta.DESCRIPTION, "プレイヤーをマーカーにテレポートさせます。")
                .argument(StringArgument
                    .<CommandSender>newBuilder("playerOrMarkerName") // この実装気に入らないけどこうしないと動かない
                    .withSuggestionsProvider(this::suggestPlayerOrMarkerNames))
                .argument(StringArgument
                    .<CommandSender>newBuilder("markerName")
                    .asOptional()
                    .withSuggestionsProvider(this::suggestMarkerNames))
                .handler(this::teleportMarker)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "マーカーを追加します。")
                .senderType(Player.class)
                .literal("add", "set")
                .argument(StringArgument.of("markerName"))
                .argument(StringArgument
                    .<CommandSender>newBuilder("markerType")
                    .withSuggestionsProvider(this::suggestMarkerTypes))
                .argument(StringArgument
                    .<CommandSender>newBuilder("markerIcon")
                    .asOptional()
                    .withSuggestionsProvider(this::suggestMarkerIcons))
                .handler(this::addMarker)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "マーカーを削除します。")
                .senderType(Player.class)
                .literal("del", "remove", "rem")
                .argument(StringArgument
                    .<CommandSender>newBuilder("markerName")
                    .withSuggestionsProvider(this::suggestMarkerNames))
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
                .meta(CommandMeta.DESCRIPTION, "マーカーの一覧を表示します。")
                .senderType(Player.class)
                .literal("list")
                .argument(IntegerArgument
                    .<CommandSender>newBuilder("page")
                    .withMin(1)
                    .asOptional())
                .handler(this::listMarkers)
                .build()
        );
    }

    void teleportMarker(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        String inputPlayerOrMarkerName = context.get("playerOrMarkerName");
        String _inputMarkerName = context.getOrDefault("markerName", null);

        // /dt <Marker>
        // /dt <Player> <Marker>
        String inputPlayerName = _inputMarkerName != null ? inputPlayerOrMarkerName : null;
        String inputMarkerName = _inputMarkerName == null ? inputPlayerOrMarkerName : _inputMarkerName;

        if (!isEnabledPlugin("dynmap")) {
            SendMessage(sender, details(), "Dynmapプラグインが動作していないため、このコマンドは使用できません。");
            return;
        }

        if (inputPlayerName == null && !(sender instanceof Player)) {
            // プレイヤー以外かつマーカー名しか指定していない
            SendMessage(sender, details(), "このコマンドはプレイヤーから実行してください。(ターゲットプレイヤーが指定されていません)");
            return;
        }

        Player teleportPlayer;
        if (inputPlayerName != null) {
            Bukkit.getServer().sendMessage(Component.text(inputPlayerName));
            teleportPlayer = Bukkit.getPlayerExact(inputPlayerName);
            if (teleportPlayer == null) {
                SendMessage(sender, details(), "指定されたプレイヤーは存在しないか、オンラインではありません。");
                return;
            }
        } else {
            teleportPlayer = (Player) context.getSender();
        }

        DynmapAPI dynmapAPI = getDynmapAPI();
        MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();

        Set<Marker> markers = markerAPI.getMarkerSets().stream()
            .flatMap(sets -> sets.getMarkers().stream())
            .collect(Collectors.toSet());

        Optional<Marker> matchedMarker = markers.stream()
            .filter(marker -> marker.getLabel().equals(inputMarkerName))
            .findFirst();

        if (!matchedMarker.isPresent()) {
            SendMessage(sender, details(), "指定されたマーカー名のマーカーは見つかりませんでした。");
            Optional<Marker> perhapsMarker = markers.stream()
                .filter(marker -> marker.getLabel().equalsIgnoreCase(inputMarkerName))
                .findFirst();
            perhapsMarker.ifPresent(marker -> SendMessage(sender, details(), "もしかして: " + marker.getLabel()));
            return;
        }

        teleportToMarker(sender, teleportPlayer, matchedMarker.get());
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

        if (!_marker.isPresent()) {
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

    }

    void listMarkers(CommandContext<CommandSender> context) {

    }

    /**
     * プレイヤー名とマーカー名のリストを返す
     *
     * @param context CommandContext
     * @param current 入力されている値
     * @return サジェストする文字列一覧
     */
    List<String> suggestPlayerOrMarkerNames(final CommandContext<CommandSender> context, final String current) {
        return Stream.concat(
            suggestMarkerNames(context, current).stream(),
            suggestOnlinePlayers(context, current).stream())
            .collect(Collectors.toList());
    }

    /**
     * マーカー名のリストを返す
     *
     * @param context CommandContext
     * @param current 入力されている値
     * @return サジェストする文字列一覧
     */
    List<String> suggestMarkerNames(final CommandContext<CommandSender> context, final String current) {
        if (!isEnabledPlugin("dynmap")) {
            return new ArrayList<>();
        }
        DynmapAPI dynmapAPI = getDynmapAPI();
        MarkerAPI markerAPI = dynmapAPI.getMarkerAPI();

        return markerAPI.getMarkerSets().stream()
            .flatMap(a -> a.getMarkers().stream())
            .collect(Collectors.toList()).stream()
            .map(GenericMarker::getLabel)
            .filter(label -> label.startsWith(current))
            .collect(Collectors.toList());
    }

    /**
     * マーカータイプのリストを返す
     *
     * @param context CommandContext
     * @param current 入力されている値
     * @return サジェストする文字列一覧
     */
    List<String> suggestMarkerTypes(final CommandContext<CommandSender> context, final String current) {
        if (!isEnabledPlugin("dynmap")) {
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
     * @return サジェストする文字列一覧
     */
    List<String> suggestMarkerIcons(final CommandContext<CommandSender> context, final String current) {
        if (!isEnabledPlugin("dynmap")) {
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
     * @return 存在すればTrue
     */
    boolean isExistsMarker(MarkerAPI markerAPI, String markerName) {
        return markerAPI.getMarkerSets().stream()
            .flatMap(a -> a.getMarkers().stream())
            .anyMatch(marker -> marker.getLabel().equals(markerName));
    }

    void teleportToMarker(CommandSender sender, Player player, Marker marker) {
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
        player.teleport(loc);

        SendMessage(sender, details(), String.valueOf(sender == player));

        // 可読性悪すぎ
        HoverEvent<?> senderHoverEvent =
            sender == player ? // こんなことできんの？ (期待していること: コマンド実行者とテレポートされるプレイヤーが同じかどうか調べたい)
                HoverEvent.showEntity(
                    Key.key("player"),
                    player.getUniqueId(),
                    Component.text(player.getName())) :
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
            Component.text(player.getName(), NamedTextColor.GRAY, TextDecoration.ITALIC)
                .hoverEvent(HoverEvent.showEntity(
                    Key.key("player"),
                    player.getUniqueId(),
                    Component.text(player.getName()))),
            Component.space(),
            Component.text("は"),
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
            Component.text("にワープしました]", NamedTextColor.GRAY)
        ));
    }
}
