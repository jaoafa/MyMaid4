package com.jaoafa.mymaid4.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd_Getlookloc extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "getlookloc",
            "見ているブロックの座標を提案します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "見ているブロックの座標を提案します。")
                .senderType(Player.class)
                .handler(this::suggestLoc)
                .build()
        );
    }

    void suggestLoc(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Location loc = player.getTargetBlock(null, 50).getLocation();
        String loctext = String.format("x:%d y:%d z:%d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        String copyloctext = String.format("%d %d %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        Component component = Component.text().append(
            Component.text("[クリップボードへコピー(" + loctext + ")]", Style.style().clickEvent(ClickEvent.copyToClipboard(copyloctext)).decorate(TextDecoration.UNDERLINED).color(NamedTextColor.GOLD).build())
        ).build();
        SendMessage(player, details(), component);
    }
}
