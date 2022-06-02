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

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.google.common.collect.Sets;
import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.ConvLoc;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Cmd_ConvLoc extends MyMaidLibrary implements CommandPremise {

    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "convloc",
            "コマンドブロックのコマンドの座標指定を「絶対座標」と「相対座標」で相互変換します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "見ているコマンドブロックのコマンドを「相対座標」に変換します。")
                .handler(this::lookingChangeToRelative)
                .senderType(Player.class)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "見ているコマンドブロックのコマンドを「相対座標(relative)」に変換します。relativeは短縮できます。")
                .literal("relative")
                .senderType(Player.class)
                .handler(this::lookingChangeToRelative)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "見ているコマンドブロックのコマンドを「絶対座標(absolute)」に変換します。absoluteは短縮できます。")
                .literal("absolute")
                .senderType(Player.class)
                .handler(this::lookingChangeToAbsolute)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "叩いたコマンドブロックのコマンドを「相対座標(relative)」か「絶対座標(absolute)」のいずれかに変換する棒を付与します。")
                .literal("click", "stick", "c")
                .argument(StringArgument
                    .<CommandSender>newBuilder("type")
                    .asOptionalWithDefault("relative")
                    .withSuggestionsProvider(this::suggestType)
                    .build())
                .handler(this::giveConvLocStick)
                .senderType(Player.class)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "WorldEditで選択した範囲にあるコマンドブロックの座標を「相対座標(relative)」か「絶対座標(absolute)」のいずれかに変換します。")
                .literal("select")
                .argument(StringArgument
                    .<CommandSender>newBuilder("type")
                    .withSuggestionsProvider(this::suggestType)
                    .asOptionalWithDefault("relative")
                    .build())
                .senderType(Player.class)
                .handler(this::changeToggleSelectArea)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "変換したコマンドブロックをひとつ戻します。")
                .literal("undo")
                .handler(this::undo)
                .build()
        );
    }

    void lookingChangeToRelative(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Set<Material> materials = Sets.newHashSet(Material.values());
        materials.remove(Material.COMMAND_BLOCK);
        materials.remove(Material.CHAIN_COMMAND_BLOCK);
        materials.remove(Material.REPEATING_COMMAND_BLOCK);
        Block targetBlock = player.getTargetBlock(materials, 10);

        new ConvLoc().replace(player, Collections.singletonList(targetBlock), true);
    }


    void lookingChangeToAbsolute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Set<Material> materials = Sets.newHashSet(Material.values());
        materials.remove(Material.COMMAND_BLOCK);
        materials.remove(Material.CHAIN_COMMAND_BLOCK);
        materials.remove(Material.REPEATING_COMMAND_BLOCK);
        Block targetBlock = player.getTargetBlock(materials, 10);

        new ConvLoc().replace(player, Collections.singletonList(targetBlock), false);
    }

    void giveConvLocStick(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String type = context.get("type");

        ItemStack is = new ItemStack(Material.STICK);
        ItemMeta meta = is.getItemMeta();
        meta.displayName(Component.text("ConvLocStick : %s".formatted("relative".startsWith(type.toLowerCase()) ? "RELATIVE" : "ABSOLUTE"), NamedTextColor.YELLOW));
        meta.addEnchant(Enchantment.DURABILITY, 1, false);
        is.setItemMeta(meta);

        PlayerInventory inv = player.getInventory();
        ItemStack main = inv.getItemInMainHand();

        inv.setItemInMainHand(is);
        SendMessage(player, details(), "コマンドブロックのコマンドを" + ("relative".startsWith(type.toLowerCase()) ? "相対" : "絶対") + "座標に変換する棒をメインハンドのアイテムと置きかえました。");

        if (main.getType() != Material.AIR) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getLocation().getWorld().dropItem(player.getLocation(), main);
                SendMessage(player, details(), "インベントリがいっぱいだったため、既に持っていたアイテムはあなたの足元にドロップしました。");
            } else {
                inv.addItem(main);
            }
        }
    }

    void changeToggleSelectArea(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String type = context.get("type");

        WorldEditPlugin we = Main.getWorldEdit();

        if (we == null) {
            SendMessage(player, details(), "WorldEditプラグインが動作していないため、このコマンドを使用できません。");
            return;
        }

        World selectionWorld;
        Region region;
        try {
            selectionWorld = we.getSession(player).getSelectionWorld();
            if (selectionWorld == null) {
                throw new IncompleteRegionException();
            }
            region = we.getSession(player).getSelection(selectionWorld);
            if (region == null) {
                throw new IncompleteRegionException();
            }
        } catch (IncompleteRegionException e) {
            SendMessage(player, details(), "WorldEditで範囲を指定してください。");
            return;
        }

        org.bukkit.@Nullable World world = Bukkit.getWorld(selectionWorld.getName());
        if (world == null) {
            SendMessage(player, details(), "選択しているワールドを取得できませんでした。");
            return;
        }
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        ArrayList<Block> blocks = new ArrayList<>();
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    blocks.add(block);
                }
            }
        }
        new ConvLoc().replace(player, blocks, "relative".startsWith(type.toLowerCase()));
    }

    void undo(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        new ConvLoc().undo(player);
    }

    List<String> suggestType(final CommandContext<CommandSender> context, final String current) {
        return Stream
            .of("relative", "absolute")
            .filter(s -> s.toLowerCase().startsWith(current.toLowerCase()))
            .collect(Collectors.toList());
    }
}