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
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class Cmd_Var extends MyMaidLibrary implements CommandPremise {
    final Pattern pattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_.]{2,}$");

    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "var",
            "変数に関することを利用できます。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "変数を設定(代入)します。")
                .literal("text", "set")
                .argument(StringArgument
                    .<CommandSender>newBuilder("key")
                    .withSuggestionsProvider(this::suggestVariableNames), ArgumentDescription.of("変数名"))
                .argument(StringArgument.of("value"), ArgumentDescription.of("代入する値"))
                .handler(this::setVariable)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "加算し、結果を変数に代入します。")
                .literal("plus", "add")
                .argument(StringArgument
                    .<CommandSender>newBuilder("setToKey")
                    .withSuggestionsProvider(this::suggestVariableNames), ArgumentDescription.of("結果を代入する変数名"))
                .argument(StringArgument
                    .<CommandSender>newBuilder("keyOrValue1")
                    .withSuggestionsProvider(this::suggestVariableNames), ArgumentDescription.of("加算する変数名もしくは値"))
                .argument(StringArgument
                    .<CommandSender>newBuilder("keyOrValue2")
                    .withSuggestionsProvider(this::suggestVariableNames), ArgumentDescription.of("加算する変数名もしくは値"))
                .handler(c -> processVariable(c, MathSign.PLUS))
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "減算し、結果を変数に代入します。")
                .literal("minus", "remove", "rem", "rm", "subtraction", "sub")
                .argument(StringArgument
                    .<CommandSender>newBuilder("setToKey")
                    .withSuggestionsProvider(this::suggestVariableNames), ArgumentDescription.of("結果を代入する変数名"))
                .argument(StringArgument
                    .<CommandSender>newBuilder("keyOrValue1")
                    .withSuggestionsProvider(this::suggestVariableNames), ArgumentDescription.of("減算される変数名もしくは値"))
                .argument(StringArgument
                    .<CommandSender>newBuilder("keyOrValue2")
                    .withSuggestionsProvider(this::suggestVariableNames), ArgumentDescription.of("減算する変数名もしくは値"))
                .handler(c -> processVariable(c, MathSign.MINUS))
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "乗算し、結果を変数に代入します。")
                .literal("multiply", "multi")
                .argument(StringArgument
                    .<CommandSender>newBuilder("setToKey")
                    .withSuggestionsProvider(this::suggestVariableNames), ArgumentDescription.of("結果を代入する変数名"))
                .argument(StringArgument
                    .<CommandSender>newBuilder("keyOrValue1")
                    .withSuggestionsProvider(this::suggestVariableNames), ArgumentDescription.of("乗算される変数名もしくは値"))
                .argument(StringArgument
                    .<CommandSender>newBuilder("keyOrValue2")
                    .withSuggestionsProvider(this::suggestVariableNames), ArgumentDescription.of("乗算する変数名もしくは値"))
                .handler(c -> processVariable(c, MathSign.MULTIPLY))
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "除算し、結果を変数に代入します。")
                .literal("division", "div")
                .argument(StringArgument
                    .<CommandSender>newBuilder("setToKey")
                    .withSuggestionsProvider(this::suggestVariableNames), ArgumentDescription.of("結果を代入する変数名"))
                .argument(StringArgument
                    .<CommandSender>newBuilder("keyOrValue1")
                    .withSuggestionsProvider(this::suggestVariableNames), ArgumentDescription.of("除算される変数名もしくは値"))
                .argument(StringArgument
                    .<CommandSender>newBuilder("keyOrValue2")
                    .withSuggestionsProvider(this::suggestVariableNames), ArgumentDescription.of("乗算する変数名もしくは値"))
                .handler(c -> processVariable(c, MathSign.DIVIDE))
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "計算し、結果を変数に代入します。")
                .literal("calc")
                .argument(StringArgument
                    .<CommandSender>newBuilder("setToKey")
                    .withSuggestionsProvider(this::suggestVariableNames), ArgumentDescription.of("結果を代入する変数名"))
                .argument(StringArgument
                    .<CommandSender>newBuilder("keyOrValue1")
                    .withSuggestionsProvider(this::suggestVariableNames), ArgumentDescription.of("計算される変数名もしくは値"))
                .argument(StringArgument
                    .<CommandSender>newBuilder("mathSign")
                    .withSuggestionsProvider((context, current) -> Arrays.asList("+", "-", "*", "/")), ArgumentDescription.of("計算記号"))
                .argument(StringArgument
                    .<CommandSender>newBuilder("keyOrValue2")
                    .withSuggestionsProvider(this::suggestVariableNames), ArgumentDescription.of("計算する変数名もしくは値"))
                .handler(this::calcVariable)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "指定されたキーの値を出力します。")
                .literal("output", "out", "view")
                .argument(StringArgument
                    .<CommandSender>newBuilder("key")
                    .withSuggestionsProvider(this::suggestVariableNames), ArgumentDescription.of("変数名"))
                .handler(this::outputVariable)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "キーの一覧を表示します。")
                .literal("list")
                .argument(IntegerArgument.optional("page", 1), ArgumentDescription.of("ページ"))
                .handler(this::listVariable)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "キーを削除します。")
                .literal("clear", "reset")
                .argument(StringArgument
                    .<CommandSender>newBuilder("key")
                    .withSuggestionsProvider(this::suggestVariableNames), ArgumentDescription.of("削除する変数の変数名"))
                .handler(this::clearVariable)
                .build()
        );
    }

    void setVariable(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        String key = context.get("key");
        String value = context.get("value");

        if (!pattern.matcher(key).matches()) {
            SendMessage(sender, details(), "変数名が不適切です。変数名は1文字目は半角大/小英字、2文字目以降は半角大/小英数字・アンダーバー・ピリオドである必要があり、3文字以上である必要があります。");
            return;
        }

        if (value.startsWith("@")) {
            List<Entity> entities = Bukkit.selectEntities(sender, value);
            if (entities.size() != 1) {
                throw new IllegalArgumentException(MessageFormat.format("セレクター「{0}」の対象となるエンティティが見つからないか、2つ以上存在するため動作しません。",
                    value));
            }
            value = entities.get(0).getName();
        }

        VariableManager vm = MyMaidData.getVariableManager();
        vm.set(key, value);
        SendMessage(sender, details(), "変数「" + key + "」に値「" + value + "」を代入しました。");
    }

    void processVariable(CommandContext<CommandSender> context, MathSign unit) {
        CommandSender sender = context.getSender();
        String setToKey = context.get("setToKey");
        String keyOrValue1 = context.get("keyOrValue1");
        String keyOrValue2 = context.get("keyOrValue2");

        if (!pattern.matcher(setToKey).matches()) {
            SendMessage(sender, details(), "変数名が不適切です。変数名は1文字目は半角大/小英字、2文字目以降は半角大/小英数字・アンダーバー・ピリオドである必要があり、3文字以上である必要があります。");
            return;
        }

        int value1;
        try {
            value1 = getValue(keyOrValue1);
        } catch (IllegalArgumentException e) {
            SendMessage(sender, details(), e.getMessage());
            return;
        }

        int value2;
        try {
            value2 = getValue(keyOrValue2);
        } catch (IllegalArgumentException e) {
            SendMessage(sender, details(), e.getMessage());
            return;
        }

        calcProcess(sender, setToKey, value1, value2, unit);
    }

    void calcVariable(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        String setToKey = context.get("setToKey");
        String keyOrValue1 = context.get("keyOrValue1");
        String mathSign = context.get("mathSign");
        String keyOrValue2 = context.get("keyOrValue2");

        if (!pattern.matcher(setToKey).matches()) {
            SendMessage(sender, details(), "変数名が不適切です。変数名は1文字目は半角大/小英字、2文字目以降は半角大/小英数字・アンダーバー・ピリオドである必要があり、3文字以上である必要があります。");
            return;
        }

        int value1;
        try {
            value1 = getValue(keyOrValue1);
        } catch (IllegalArgumentException e) {
            SendMessage(sender, details(), e.getMessage());
            return;
        }

        int value2;
        try {
            value2 = getValue(keyOrValue2);
        } catch (IllegalArgumentException e) {
            SendMessage(sender, details(), e.getMessage());
            return;
        }

        if (MathSign.fromId(mathSign).isEmpty()) return;

        calcProcess(sender, setToKey, value1, value2, MathSign.fromId(mathSign).get());
    }

    void outputVariable(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        String key = context.get("key");

        if (!pattern.matcher(key).matches()) {
            SendMessage(sender, details(), "変数名が不適切です。変数名は1文字目は半角大/小英字、2文字目以降は半角大/小英数字・アンダーバー・ピリオドである必要があり、3文字以上である必要があります。");
            return;
        }

        VariableManager vm = MyMaidData.getVariableManager();
        if (!vm.isDefined(key)) {
            SendMessage(sender, details(), "変数「" + key + "」は定義されていません。");
            return;
        }
        sender.sendMessage(vm.getString(key));
    }

    void listVariable(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        int page = context.get("page");
        if (page <= 0) {
            SendMessage(sender, details(), "ページは1以上を指定する必要があります。");
            return;
        }

        VariableManager vm = MyMaidData.getVariableManager();
        vm
            .getVariables()
            .entrySet()
            .stream()
            .skip((page - 1) * 10L)
            .limit(10)
            .forEach(
                entry -> SendMessage(sender, details(), entry.getKey() + ": " + entry.getValue())
            );
    }

    void clearVariable(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        String key = context.get("key");
        VariableManager vm = MyMaidData.getVariableManager();
        if (!vm.isDefined(key)) {
            SendMessage(sender, details(), "変数「" + key + "」は定義されていません。");
            return;
        }

        SendMessage(sender, details(), "変数「" + key + "」を削除" + (vm.remove(key) ? "しました" : "できませんでした") + "。");
    }

    int getValue(String keyOrValue) {
        VariableManager vm = MyMaidData.getVariableManager();
        if (vm.isDefined(keyOrValue)) {
            if (!isInt(vm.getString(keyOrValue))) {
                throw new IllegalArgumentException(MessageFormat.format("キー「{0}」の値「{1}」は数値ではありません。",
                    keyOrValue,
                    vm.getString(keyOrValue)));
            }
            return vm.getInt(keyOrValue);
        } else {
            if (!isInt(keyOrValue)) {
                throw new IllegalArgumentException(MessageFormat.format("値「{0}」は数値ではないか、変数が定義されていません。",
                    keyOrValue));
            }
            return Integer.parseInt(keyOrValue);
        }
    }

    void calcProcess(CommandSender sender, String key, int value1, int value2, MathSign mathSign) {
        VariableManager vm = MyMaidData.getVariableManager();

        int result = switch (mathSign) {
            case PLUS -> value1 + value2;
            case MINUS -> value1 - value2;
            case MULTIPLY -> value1 * value2;
            case DIVIDE -> value1 / value2;
        };

        vm.set(key, result);
        SendMessage(sender, details(), String.format("「%d」%s「%d」を%s結果である「%d」をキー「%s」にセットしました。", value1, mathSign.andOr, value2, mathSign.what, result, key));
    }

    List<String> suggestVariableNames(final CommandContext<CommandSender> context, final String current) {
        return new ArrayList<>(MyMaidData.getVariableManager().getVariables().keySet());
    }

    enum MathSign {
        PLUS("+", "と", "足した"),
        MINUS("-", "から", "引いた"),
        MULTIPLY("*", "と", "掛けた"),
        DIVIDE("/", "から", "割った");

        final String signId;
        final String andOr;
        final String what;

        MathSign(String signId, String andOr, String what) {
            this.signId = signId;
            this.andOr = andOr;
            this.what = what;
        }

        public static Optional<MathSign> fromId(String signId) {
            return Arrays.stream(values()).filter(u -> u.signId.equals(signId)).findFirst();
        }
    }
}
