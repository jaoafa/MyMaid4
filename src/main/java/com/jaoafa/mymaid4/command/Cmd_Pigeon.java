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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Cmd_Pigeon extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "pigeon",
            Collections.singletonList("messenger"),
            "伝書鳩ちゃんにお願いを伝えます。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "伝書鳩ちゃんにスピーカーを追加してもらいます。")
                .literal("speaker", "speakers")
                .literal("add")
                .argument(StringArgument.greedy("speaker"), ArgumentDescription.of("スピーカー名"))
                .handler(this::addSpeaker)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "伝書鳩ちゃんにメッセージを追加してもらいます。")
                .literal("messages", "message", "msg")
                .literal("add")
                .argument(StringArgument.greedy("message"), ArgumentDescription.of("メッセージ内容"))
                .handler(this::addMessage)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "伝書鳩ちゃんにスピーカーを削除してもらいます。")
                .literal("speaker", "speakers")
                .literal("remove", "rem", "delete", "del")
                .argument(StringArgument
                    .<CommandSender>newBuilder("speaker")
                    .asOptional()
                    .withSuggestionsProvider(this::suggestSpeakers), ArgumentDescription.of("スピーカー名もしくはID"))
                .handler(this::removeSpeaker)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "伝書鳩ちゃんにメッセージを削除してもらいます。")
                .literal("messages", "message", "msg")
                .literal("remove", "rem", "delete", "del")
                .argument(StringArgument
                    .<CommandSender>newBuilder("message")
                    .asOptional()
                    .withSuggestionsProvider(this::suggestMessages), ArgumentDescription.of("メッセージ内容"))
                .handler(this::removeMessage)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "伝書鳩ちゃんにスピーカーが誰がいるか聞きます。")
                .literal("speaker", "speakers")
                .literal("list")
                .handler(this::listSpeakers)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "伝書鳩ちゃんにメッセージが何があるか聞きます。")
                .literal("messages", "msg")
                .literal("list")
                .handler(this::listMessages)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "伝書鳩ちゃんにメッセージを配信してもらいます。")
                .literal("broadcast")
                .argument(IntegerArgument
                    .<CommandSender>newBuilder("messageId")
                    .asOptional()
                    .withSuggestionsProvider(this::suggestMessageIds), ArgumentDescription.of("メッセージID"))
                .handler(this::broadcast)
                .build()
        );
    }

    void addSpeaker(CommandContext<CommandSender> context) {
        if (isUnavailable(context)) return;
        CommandSender sender = context.getSender();

        String speaker = context.getOrDefault("speaker", null);
        if (speaker == null) {
            speakBird(sender, "スピーカーを入力してね。");
            return;
        }

        CarrierPigeon carrierPigeon = MyMaidData.getCarrierPigeon();
        if (carrierPigeon.existsSpeaker(speaker)) {
            speakBird(sender, "教えてくれたスピーカーは既にあるみたいです！ほかのを考えてみてね。");
            return;
        }

        try {
            carrierPigeon.addSpeaker(speaker);
            speakBird(sender, "教えてくれたスピーカーを追加しました！どうもありがとう。");
        } catch (IOException e) {
            speakBird(sender, "追加しようとしたけどエラーが出ちゃいました...。少し待ってからもう一度試してみてね。");
            reportError(getClass(), e);
        }
    }

    void addMessage(CommandContext<CommandSender> context) {
        if (isUnavailable(context)) return;
        CommandSender sender = context.getSender();

        String message = context.getOrDefault("message", null);
        if (message == null) {
            speakBird(sender, "メッセージを入力してね。");
            return;
        }

        CarrierPigeon carrierPigeon = MyMaidData.getCarrierPigeon();
        if (carrierPigeon.existsMessage(message)) {
            speakBird(sender, "教えてくれたメッセージは既にあるみたいです！ほかのを考えてみてね。");
            return;
        }

        try {
            carrierPigeon.addMessage(message);
            speakBird(sender, "教えてくれたメッセージを追加しました！どうもありがとう。");
        } catch (IOException e) {
            speakBird(sender, "追加しようとしたけどエラーが出ちゃいました...。少し待ってからもう一度試してみてね。");
            reportError(getClass(), e);
        }
    }

    void removeSpeaker(CommandContext<CommandSender> context) {
        if (isUnavailable(context)) return;
        CommandSender sender = context.getSender();

        String input = context.getOrDefault("speaker", null);
        if (input == null) {
            speakBird(sender, "スピーカーを入力してね。");
            return;
        }

        CarrierPigeon carrierPigeon = MyMaidData.getCarrierPigeon();
        if (!carrierPigeon.existsSpeaker(input) &&
            (isInt(input) && !carrierPigeon.existsSpeaker(Integer.parseInt(input)))) {
            speakBird(sender, "スピーカーを消そうとしたけど既にありませんでした...。どこいっちゃったのかな？");
            return;
        }

        int speakerId = carrierPigeon.existsSpeaker(input) ? carrierPigeon.getSpeakerId(input) : Integer.parseInt(input);
        String speaker = carrierPigeon.getSpeaker(speakerId);

        try {
            carrierPigeon.removeSpeaker(speakerId);
            speakBird(sender, String.format("スピーカー「%s」を削除しました。さよなら～(´；ω；｀)", speaker));
        } catch (IOException e) {
            speakBird(sender, "削除しようとしたけどエラーが出ちゃいました...。少し待ってからもう一度試してみてね。");
            reportError(getClass(), e);
        }
    }

    void removeMessage(CommandContext<CommandSender> context) {
        if (isUnavailable(context)) return;
        CommandSender sender = context.getSender();

        String input = context.getOrDefault("message", null);
        if (input == null) {
            speakBird(sender, "メッセージを入力してね。");
            return;
        }

        CarrierPigeon carrierPigeon = MyMaidData.getCarrierPigeon();
        if (!carrierPigeon.existsMessage(input) &&
            (isInt(input) && !carrierPigeon.existsMessage(Integer.parseInt(input)))) {
            speakBird(sender, "メッセージを消そうとしたけど既にありませんでした...。どこいっちゃったのかな？");
            return;
        }

        int messageId = carrierPigeon.existsMessage(input) ? carrierPigeon.getMessageId(input) : Integer.parseInt(input);
        String speaker = carrierPigeon.getMessage(messageId);

        try {
            carrierPigeon.removeMessage(messageId);
            speakBird(sender, String.format("メッセージ「%s」を削除しました。ばいば～い～(´；ω；｀)", speaker));
        } catch (IOException e) {
            speakBird(sender, "削除しようとしたけどエラーが出ちゃいました...。少し待ってからもう一度試してみてね。");
            reportError(getClass(), e);
        }
    }

    void listSpeakers(CommandContext<CommandSender> context) {
        if (isUnavailable(context)) return;
        CommandSender sender = context.getSender();

        CarrierPigeon carrierPigeon = MyMaidData.getCarrierPigeon();
        List<String> speakers = carrierPigeon.getSpeakers();

        speakBird(sender, String.format("今登録されているスピーカーは以下の%dつあります！", speakers.size()));
        for (int i = 0; i < speakers.size(); i++) {
            speakBird(sender, "[" + i + "] " + speakers.get(i));
        }
    }

    void listMessages(CommandContext<CommandSender> context) {
        if (isUnavailable(context)) return;
        CommandSender sender = context.getSender();

        CarrierPigeon carrierPigeon = MyMaidData.getCarrierPigeon();
        List<String> messages = carrierPigeon.getMessages();

        speakBird(sender, String.format("今登録されているメッセージは以下の%dつあります！", messages.size()));
        for (int i = 0; i < messages.size(); i++) {
            speakBird(sender, "[" + i + "] " + messages.get(i));
        }
    }

    void broadcast(CommandContext<CommandSender> context) {
        if (isUnavailable(context)) return;
        CommandSender sender = context.getSender();
        CarrierPigeon carrierPigeon = MyMaidData.getCarrierPigeon();
        Integer messageId = context.getOrDefault("messageId", null);

        if (messageId == null) {
            carrierPigeon.randomBroadcast();
            return;
        }

        if (!carrierPigeon.existsMessage(messageId)) {
            speakBird(sender, "指定されたメッセージIdのメッセージは見つかりませんでした...。");
            return;
        }
        carrierPigeon.broadcast(messageId);
    }

    boolean isUnavailable(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        if (MyMaidData.getCarrierPigeon() == null) {
            SendMessage(sender, details(), "現在、このコマンドは使用できません。");
            return true;
        }

        if (context.getSender() instanceof Player player) {
            if (!isAMR(player)) {
                speakBird(sender, "残念ながら、あなたはこのコマンドを使えないみたいです...。");
                return true;
            }
        }
        return false;
    }

    List<String> suggestSpeakers(final CommandContext<CommandSender> context, final String current) {
        if (MyMaidData.getCarrierPigeon() == null) {
            return new ArrayList<>();
        }

        // スピーカーとIdを返す
        CarrierPigeon carrierPigeon = MyMaidData.getCarrierPigeon();
        return Stream.concat(
                carrierPigeon.getSpeakers().stream(),
                IntStream.range(0, carrierPigeon.getSpeakers().size())
                    .mapToObj(Integer::toString)
            ).filter(s -> s.startsWith(current))
            .collect(Collectors.toList());
    }

    List<String> suggestMessages(final CommandContext<CommandSender> context, final String current) {
        if (MyMaidData.getCarrierPigeon() == null) {
            return new ArrayList<>();
        }

        // メッセージとIdを返す
        CarrierPigeon carrierPigeon = MyMaidData.getCarrierPigeon();
        return Stream.concat(
                carrierPigeon.getMessages().stream(),
                IntStream.range(0, carrierPigeon.getMessages().size())
                    .mapToObj(Integer::toString)
            ).filter(s -> s.startsWith(current))
            .collect(Collectors.toList());
    }

    List<String> suggestMessageIds(final CommandContext<CommandSender> context, final String current) {
        if (MyMaidData.getCarrierPigeon() == null) {
            return new ArrayList<>();
        }

        // メッセージIdのみを返す
        CarrierPigeon carrierPigeon = MyMaidData.getCarrierPigeon();
        return IntStream.range(0, carrierPigeon.getMessages().size())
            .mapToObj(String::valueOf)
            .filter(s -> s.toLowerCase().startsWith(current.toLowerCase()))
            .collect(Collectors.toList());
    }

    void speakBird(CommandSender sender, String message) {
        CarrierPigeon.speakBird(sender, message);
    }
}
