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
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConvLoc {
    Pattern LOC_PATTERN = Pattern.compile("^(~?)(-?)([.\\d]+)$");
    Pattern SELECTOR_PATTERN = Pattern.compile("^@[praes]\\[.*?]$");
    Pattern XYZ_SELECTOR_PATTERN = Pattern.compile("[^d]([xyz])=([~.\\-0-9]+)");

    public void replace(Player player, List<Block> blocks, boolean isRelative) {
        List<Replacer> replacers = new ArrayList<>();
        for (Block block : blocks) {
            Replacer replacer = new Replacer(block, isRelative);
            replacers.add(replacer);
            if (replacer.getResult() == Replacer.ReplacerResult.SUCCESS) replacer.confirm();
        }
        logging(player, replacers);
        long successCount = replacers.stream().filter(r -> r.getResult() == Replacer.ReplacerResult.SUCCESS).count();
        long noChangedCount = replacers.stream().filter(r -> r.getResult() == Replacer.ReplacerResult.NO_CHANGED).count();
        long failedCount = replacers.stream().filter(r -> r.getResult() == Replacer.ReplacerResult.FAILED_CONVERT).count();
        player.sendMessage(Component.join(JoinConfiguration.noSeparators(),
            Component.text("[ConvLoc] "),
            Component.text("コマンドブロックのコマンドを「" + (
                isRelative ? "相対座標" : "絶対座標"
            ) + "」に変更" + (
                successCount != 0 ? "しました。" : "できませんでした。"
            ), successCount != 0 ? NamedTextColor.GREEN : NamedTextColor.RED)
        ));
        player.sendMessage(Component.join(JoinConfiguration.noSeparators(),
            Component.text("[ConvLoc] "),
            Component.text("成功: %d / 変更無(対象外): %d / 失敗: %d".formatted(successCount, noChangedCount, failedCount), NamedTextColor.GREEN)
        ));
    }

    public void undo(Player player) {
        try {
            Path path = Path.of(Main.getJavaPlugin().getDataFolder().getAbsolutePath(), "convloc", "logs", player.getUniqueId() + ".json");
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                player.sendMessage(Component.join(JoinConfiguration.noSeparators(),
                    Component.text("[ConvLoc] "),
                    Component.text("変換ログが見つかりませんでした。", NamedTextColor.RED)
                ));
                return;
            }
            JSONArray array = new JSONArray(Files.readString(path));
            long latestTime = 0;
            int latestKey = 0;
            JSONObject latest = null;
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                long time = obj.getLong("time");
                if (time > latestTime) {
                    latestTime = time;
                    latestKey = i;
                    latest = obj;
                }
            }
            if (latest == null) {
                player.sendMessage(Component.join(JoinConfiguration.noSeparators(),
                    Component.text("[ConvLoc] "),
                    Component.text("変換ログが見つかりませんでした。", NamedTextColor.RED)
                ));
                return;
            }
            JSONArray changes = latest.getJSONArray("changes");
            int changedCount = 0;
            for (int i = 0; i < changes.length(); i++) {
                JSONObject obj = changes.getJSONObject(i);
                JSONObject location = obj.getJSONObject("location");
                World world = Bukkit.getWorld(location.getString("world"));
                if (world == null) {
                    continue;
                }
                Block block = world.getBlockAt(location.getInt("x"), location.getInt("y"), location.getInt("z"));
                if (block.getType() != Material.COMMAND_BLOCK && block.getType() != Material.CHAIN_COMMAND_BLOCK && block.getType() != Material.REPEATING_COMMAND_BLOCK) {
                    continue;
                }
                CommandBlock cb = (CommandBlock) block.getState();
                if (!cb.getCommand().equals(obj.getString("newCommand"))) {
                    continue;
                }
                cb.setCommand(obj.getString("oldCommand"));
                cb.update();
                changedCount++;
            }
            array.remove(latestKey);
            Files.writeString(path, array.toString(2));

            player.sendMessage(Component.join(JoinConfiguration.noSeparators(),
                Component.text("[ConvLoc] "),
                Component.text("%d件の座標変換をもとに戻しました。".formatted(changedCount), NamedTextColor.GREEN)
            ));
        } catch (IOException ignored) {
        }
    }

    class Replacer {
        Block block;
        boolean isRelative;

        ReplacerResult result;
        String oldCommand;
        String newCommand = null;
        CommandBlock cb;

        Replacer(Block block, boolean isRelative) {
            this.block = block;
            this.isRelative = isRelative;

            dryRun();
        }

        void dryRun() {
            if (block.getType() != Material.COMMAND_BLOCK && block.getType() != Material.CHAIN_COMMAND_BLOCK && block.getType() != Material.REPEATING_COMMAND_BLOCK) {
                result = ReplacerResult.NOT_TARGET_MATERIAL;
                return;
            }
            if (!(block.getState() instanceof CommandBlock cb)) {
                result = ReplacerResult.FAILED_GET_STATE;
                return;
            }
            this.cb = cb;
            String command = cb.getCommand();
            oldCommand = command;
            if (command.isEmpty()) {
                result = ReplacerResult.COMMAND_EMPTY;
                return;
            }
            if (!command.contains(" ")) {
                result = ReplacerResult.COMMAND_NO_ARGUMENT;
                return;
            }

            newCommand = convert(block.getLocation(), command, isRelative);
            if (newCommand == null) {
                result = ReplacerResult.FAILED_CONVERT;
                return;
            }
            if (newCommand.equals(oldCommand)) {
                result = ReplacerResult.NO_CHANGED;
                return;
            }
            result = ReplacerResult.SUCCESS;
        }

        public void confirm() {
            cb.setCommand(newCommand);
            cb.update(true);
        }

        public ReplacerResult getResult() {
            return result;
        }

        public String getOldCommand() {
            return oldCommand;
        }

        public String getNewCommand() {
            return newCommand;
        }

        public Block getBlock() {
            return block;
        }

        String convert(Location loc, String command, boolean toRelative) {
            String _baseCommand = command.split(" ")[0].trim();
            String baseCommand = _baseCommand;
            if (baseCommand.charAt(0) == '$') baseCommand = baseCommand.substring(1);
            if (baseCommand.charAt(0) == '/') baseCommand = baseCommand.substring(1);
            List<String> args = Arrays.asList(Arrays.copyOfRange(command.split(" "), 1, command.split(" ").length));
            try {
                LinkedList<String> new_args = new LinkedList<>();
                List<String> lines = Files.readAllLines(Paths.get(Main.getJavaPlugin().getDataFolder().getAbsolutePath(), "command_sheet.txt"));
                List<String> sheet_args = null;
                for (String line : lines) {
                    String sheet_baseCommand = line.split(" ")[0].trim();
                    List<String> _sheet_args = Arrays.asList(Arrays.copyOfRange(line.split(" "), 1, line.split(" ").length));

                    if (!baseCommand.equalsIgnoreCase(sheet_baseCommand)) {
                        continue;
                    }
                    sheet_args = _sheet_args;
                    break;
                }
                for (int i = 0; i < args.size(); i++) {
                    String arg = args.get(i);
                    if (SELECTOR_PATTERN.matcher(arg).matches()) {
                        // セレクター
                        Matcher xyz = XYZ_SELECTOR_PATTERN.matcher(arg);
                        while (xyz.find()) {
                            String selector_key = xyz.group(1);
                            String selector_value = xyz.group(2);

                            if (selector_key.equalsIgnoreCase("x")) {
                                String replaced = toRelative ? toRelative(selector_value, loc.getBlockX()) : toAbsolute(selector_value, loc.getBlockX());
                                arg = arg.replace(selector_key + "=" + selector_value, selector_key + "=" + replaced);
                            } else if (selector_key.equalsIgnoreCase("y")) {
                                String replaced = toRelative ? toRelative(selector_value, loc.getBlockY()) : toAbsolute(selector_value, loc.getBlockY());
                                arg = arg.replace(selector_key + "=" + selector_value, selector_key + "=" + replaced);
                            } else if (selector_key.equalsIgnoreCase("z")) {
                                String replaced = toRelative ? toRelative(selector_value, loc.getBlockZ()) : toAbsolute(selector_value, loc.getBlockZ());
                                arg = arg.replace(selector_key + "=" + selector_value, selector_key + "=" + replaced);
                            }
                        }
                    }
                    if (sheet_args == null) {
                        new_args.add(arg);
                        continue;
                    }
                    if (i >= sheet_args.size()) {
                        new_args.add(arg);
                        continue;
                    }
                    String sheet_arg = sheet_args.get(i);
                    if (sheet_arg.equals("%N")) {
                        // skip
                        new_args.add(arg);
                        continue;
                    }
                    if (sheet_arg.equals("%X")) {
                        // x
                        String replaced = toRelative ? toRelative(arg, loc.getBlockX()) : toAbsolute(arg, loc.getBlockX());
                        new_args.add(replaced);
                        continue;
                    }
                    if (sheet_arg.equals("%Y")) {
                        // y
                        String replaced = toRelative ? toRelative(arg, loc.getBlockY()) : toAbsolute(arg, loc.getBlockY());
                        new_args.add(replaced);
                        continue;
                    }
                    if (sheet_arg.equals("%Z")) {
                        // z
                        String replaced = toRelative ? toRelative(arg, loc.getBlockZ()) : toAbsolute(arg, loc.getBlockZ());
                        new_args.add(replaced);
                        continue;
                    }
                    new_args.add(arg);
                }
                return _baseCommand + " " + String.join(" ", new_args);
            } catch (IOException e) {
                return null;
            }
        }

        enum ReplacerResult {
            /** 置換成功 */
            SUCCESS,
            /** 置換スキップ: 対象のマテリアル(コマンドブロック)ではない */
            NOT_TARGET_MATERIAL,
            /** 置換失敗: コマンドブロック情報の取得に失敗 */
            FAILED_GET_STATE,
            /** 置換失敗: コマンドが空 */
            COMMAND_EMPTY,
            /** 置換失敗: コマンドに引数がない */
            COMMAND_NO_ARGUMENT,
            /** 置換失敗: 変化なし (対象コマンド以外?) */
            NO_CHANGED,
            /** 置換失敗: 変換処理に失敗 */
            FAILED_CONVERT
        }

        /**
         * 絶対座標数値から相対座標数値に変換する。
         *
         * @param xyz     絶対座標数値 (103)
         * @param cmb_xyz コマンドブロックの位置座標 (100)
         *
         * @return 相対座標数値 (~3)
         */
        String toRelative(String xyz, int cmb_xyz) {
            Matcher matcher = LOC_PATTERN.matcher(xyz);
            if (!matcher.matches()) {
                return xyz;
            }
            if (matcher.groupCount() != 3) {
                return xyz;
            }
            if (matcher.group(1).equals("~")) {
                // 既に相対座標数値
                return xyz;
            }
            double i = Double.parseDouble(matcher.group(2) + matcher.group(3));

            if ((i - cmb_xyz) == 0) return "~";
            return "~" + (i - cmb_xyz);
        }

        /**
         * 相対座標数値から絶対座標数値に変換する。
         *
         * @param xyz     相対座標数値 (~100)
         * @param cmb_xyz コマンドブロックの位置座標 (103)
         *
         * @return 絶対座標数値 (3)
         */
        String toAbsolute(String xyz, int cmb_xyz) {
            if (xyz.equals("~")) {
                return String.valueOf(cmb_xyz);
            }
            Matcher matcher = LOC_PATTERN.matcher(xyz);
            if (!matcher.matches()) {
                return xyz;
            }
            if (matcher.groupCount() != 3) {
                return xyz;
            }
            if (matcher.group(1).isEmpty()) {
                // 既に絶対座標数値
                return xyz;
            }
            double i = matcher.group(3).isEmpty() ? 0 : Double.parseDouble(matcher.group(3));
            if (matcher.group(2).equals("-")) {
                i = cmb_xyz - i;
            } else {
                i = cmb_xyz + i;
            }

            return String.valueOf(i);
        }
    }

    void logging(Player player, List<Replacer> replacers) {
        try {
            Path path = Path.of(Main.getJavaPlugin().getDataFolder().getAbsolutePath(), "convloc", "logs", player.getUniqueId() + ".json");
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            JSONArray array = new JSONArray();
            if (Files.exists(path)) {
                array = new JSONArray(Files.readString(path));
            }
            JSONArray changes = new JSONArray();
            for (Replacer replacer : replacers) {
                if (replacer.getResult() != Replacer.ReplacerResult.SUCCESS) {
                    continue;
                }
                Location loc = replacer.getBlock().getLocation();
                changes.put(new JSONObject()
                    .put("oldCommand", replacer.getOldCommand())
                    .put("newCommand", replacer.getNewCommand())
                    .put("location", new JSONObject()
                        .put("world", loc.getWorld().getName())
                        .put("x", loc.getX())
                        .put("y", loc.getY())
                        .put("z", loc.getZ())
                    ));
            }
            if (changes.length() == 0) {
                return;
            }
            array.put(new JSONObject()
                .put("changes", changes)
                .put("time", System.currentTimeMillis())
            );
            Files.writeString(Path.of(Main.getJavaPlugin().getDataFolder().getAbsolutePath(), "convloc", "logs", player.getUniqueId() + ".json"), array.toString(2));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
