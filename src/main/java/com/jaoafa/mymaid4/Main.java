/*
 * jaoLicense
 *
 * Copyright (c) 2021 jao Minecraft Server
 *
 * The following license applies to this project: jaoLicense
 *
 * Japanese: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE.md
 * English: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE-en.md
 */

package com.jaoafa.mymaid4;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.CommandComponent;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import com.jaoafa.mymaid4.httpServer.MyMaidServer;
import com.jaoafa.mymaid4.lib.*;
import com.jaoafa.mymaid4.tasks.Task_Pigeon;
import com.jaoafa.mymaid4.tasks.Task_TabList;
import net.dv8tion.jda.api.JDABuilder;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.ComponentMessageThrowable;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Main extends JavaPlugin {
    private static Main Main = null;
    private static MyMaidConfig config = null;
    private BukkitAudiences adventure;
    private MinecraftHelp<CommandSender> minecraftHelp;

    @Override
    public void onEnable() {
        Main = this;

        config = new MyMaidConfig();
        config.init();
        if (!isEnabled())
            return;

        CarrierPigeon carrierPigeon = new CarrierPigeon(new File(this.getDataFolder(), "carrierPigeon.yml"));
        MyMaidData.setCarrierPigeon(carrierPigeon);

        registerCommand();
        if (!isEnabled())
            return;

        registerEvent();

        scheduleTasks();

        MyMaidData.setBlacklist(new Blacklist());
    }

    private static Component convertCause(final Throwable throwable) {
        final Component msg = ComponentMessageThrowable.getOrConvertMessage(throwable);
        return msg != null ? msg : Component.text("null");
    }

    @Override
    public void onDisable() {
        if (config.getJDA() != null) {
            config.getJDA().getEventManager().getRegisteredListeners()
                .forEach(listener -> config.getJDA().getEventManager().unregister(listener));
            config.getJDA().shutdownNow();
        }
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
        MyMaidServer.stopServer();
    }

    private void registerEvent() {
        getLogger().info("----- registerEvent -----");

        JSONArray events = new JSONArray();
        try {
            ClassFinder classFinder = new ClassFinder(this.getClassLoader());
            for (Class<?> clazz : classFinder.findClasses("com.jaoafa.mymaid4.event")) {
                if (!clazz.getName().startsWith("com.jaoafa.mymaid4.event.Event_")) {
                    continue;
                }
                if (clazz.getEnclosingClass() != null) {
                    continue;
                }
                if (clazz.getName().contains("$")) {
                    continue;
                }
                String name = clazz.getName().substring("com.jaoafa.mymaid4.event.Event_".length())
                    .toLowerCase();
                try {
                    Constructor<?> construct = clazz.getConstructor();
                    Object instance = construct.newInstance();
                    EventPremise eventPremise = (EventPremise) instance;

                    if (!(instance instanceof Listener)) {
                        getLogger().warning(clazz.getSimpleName() + ": Listener not implemented [0]");
                        return;
                    }

                    JSONObject details = new JSONObject();
                    details.put("class", instance.getClass().getName());
                    details.put("description", eventPremise.description());
                    try {
                        Method[] methods = instance.getClass().getDeclaredMethods();
                        List<Method> eventMethods = Arrays.stream(methods)
                            .filter(m -> m.getParameterCount() == 1)
                            .filter(m -> Arrays.stream(m.getDeclaredAnnotations())
                                .anyMatch(a -> a.annotationType().equals(EventHandler.class)))
                            .collect(Collectors.toList());

                        JSONArray methodArray = new JSONArray();
                        eventMethods.forEach(method -> {
                            JSONObject obj = new JSONObject();
                            obj.put("name", method.getName());
                            obj.put("event", method.getParameterTypes()[0].getName());
                            methodArray.put(obj);
                        });
                        details.put("methods", methodArray);

                        events.put(details);
                    } catch (NoClassDefFoundError ignored) {
                    }

                    try {
                        Listener listener = (Listener) instance;
                        getServer().getPluginManager().registerEvents(listener, this);
                        getLogger().info(String.format("%s registered", clazz.getSimpleName()));
                    } catch (ClassCastException e) {
                        getLogger().warning(String.format("%s: Listener not implemented [1]", clazz.getSimpleName()));
                    }
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    getLogger().warning(String.format("%s register failed", name));
                    e.printStackTrace();
                }
            }
            MyMaidData.putGetDocsData("events", events);
        } catch (ClassNotFoundException | IOException e) {
            getLogger().warning("registerCommand failed");
            e.printStackTrace();
        }
    }

    private void registerCommand() {
        getLogger().info("----- registerCommand -----");
        final PaperCommandManager<CommandSender> manager;
        try {
            manager = new PaperCommandManager<>(this, CommandExecutionCoordinator.SimpleCoordinator.simpleCoordinator(),
                Function.identity(), Function.identity());

            // case-insensitive support (大文字小文字を区別しない)
            manager.setCommandSuggestionProcessor((context, strings) -> {
                String input = context.getInputQueue().isEmpty() ? "" : context.getInputQueue().peek().toLowerCase();
                return strings.stream()
                    .filter(s -> s.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
            });
        } catch (Exception e) {
            getLogger().warning("コマンドの登録に失敗しました。PaperCommandManagerを取得できません。");
            e.printStackTrace();
            return;
        }

        this.adventure = BukkitAudiences.create(this);
        this.minecraftHelp = new MinecraftHelp<>(
            "/mymaidhelp",
            this.adventure::sender,
            manager
        );

        // Mojangのコマンドパーサー、Brigadierを登録する
        if (manager.queryCapability(CloudBukkitCapabilities.BRIGADIER)) {
            manager.registerBrigadier();
        }

        // 非同期の引数補完
        if (manager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }

        // コマンドのエラーハンドラー
        new MinecraftExceptionHandler<CommandSender>()
            .withHandler(MinecraftExceptionHandler.ExceptionType.INVALID_SYNTAX, e ->
                Component.text().append(
                    Component.text("コマンドの構文が正しくありません。正しい構文は次の通りです: ", NamedTextColor.RED),
                    Component.text(
                        String.format(
                            "/%s",
                            ((InvalidSyntaxException) e).getCorrectSyntax()),
                        NamedTextColor.GRAY
                    ).replaceText(config -> {
                        config.match(Pattern.compile("[^\\s\\w\\-]"));
                        config.replacement(match -> match.color(NamedTextColor.WHITE));
                    })
                ).build()
            )
            .withHandler(MinecraftExceptionHandler.ExceptionType.INVALID_SENDER, e ->
                Component.text("あなたはこのコマンドを実行できません。実行できるのは次の種別の実行者のみです: ", NamedTextColor.RED)
                    .append(Component.text(
                        ((InvalidCommandSenderException) e).getRequiredSender().getSimpleName(),
                        NamedTextColor.GRAY
                    ))
            )
            .withHandler(MinecraftExceptionHandler.ExceptionType.NO_PERMISSION, e ->
                Component.text(
                    "申し訳ありませんが、このコマンドを実行する権限がありません。\n"
                        + "このコマンドを使いたい場合は、公式Discordサーバ#supportにて運営に申請してください。",
                    NamedTextColor.RED
                )
            )
            .withHandler(MinecraftExceptionHandler.ExceptionType.ARGUMENT_PARSING, e ->
                Component.text("コマンドの引数が不正です: ", NamedTextColor.RED)
                    .append(convertCause(e.getCause())
                        .colorIfAbsent(NamedTextColor.GRAY))
            )
            .withHandler(MinecraftExceptionHandler.ExceptionType.COMMAND_EXECUTION, e ->
                {
                    final Throwable cause = e.getCause();
                    cause.printStackTrace();

                    MyMaidLibrary.reportError(getClass(), e);

                    final StringWriter writer = new StringWriter();
                    cause.printStackTrace(new PrintWriter(writer));
                    final String stackTrace = writer.toString().replaceAll("\t", "    ");
                    final HoverEvent<Component> hover = HoverEvent.showText(
                        Component.text()
                            .append(convertCause(cause))
                            .append(Component.text(stackTrace))
                            .append(Component.newline())
                            .append(Component.text(
                                "エラー情報をコピーします。",
                                NamedTextColor.GRAY,
                                TextDecoration.ITALIC
                            ))
                    );
                    final ClickEvent click = ClickEvent.copyToClipboard(stackTrace);
                    return Component.text()
                        .content("コマンドを実行中に内部エラーが発生しました。ここをクリックして、内部エラーを運営にお知らせください。")
                        .color(NamedTextColor.RED)
                        .hoverEvent(hover)
                        .clickEvent(click)
                        .build();
                }
            )
            .apply(manager, adventure::sender);

        manager.command(
            manager.commandBuilder("mymaidhelp")
                .argument(StringArgument.optional("query", StringArgument.StringMode.GREEDY))
                .handler(context -> minecraftHelp.queryCommands(Objects.requireNonNull(context.getOrDefault("query", "")), context.getSender()))
        );

        JSONArray commands = new JSONArray();
        try {
            ClassFinder classFinder = new ClassFinder(this.getClassLoader());
            for (Class<?> clazz : classFinder.findClasses("com.jaoafa.mymaid4.command")) {
                if (!clazz.getName().startsWith("com.jaoafa.mymaid4.command.Cmd_")) {
                    continue;
                }
                if (clazz.getEnclosingClass() != null) {
                    continue;
                }
                if (clazz.getName().contains("$")) {
                    continue;
                }
                String commandName = clazz.getName().substring("com.jaoafa.mymaid4.command.Cmd_".length())
                    .toLowerCase();
                try {
                    Constructor<?> construct = clazz.getConstructor();
                    Object instance = construct.newInstance();
                    CommandPremise cmdPremise = (CommandPremise) instance;

                    Command.Builder<CommandSender> builder = manager.commandBuilder(
                        cmdPremise.details().getName(),
                        ArgumentDescription.of(cmdPremise.details().getDescription()),
                        cmdPremise.details().getAliases().toArray(new String[0])
                    )
                        .permission(String.format("mymaid.%s", cmdPremise.details().getName().toLowerCase()))
                        .meta(CommandMeta.DESCRIPTION, cmdPremise.details().getDescription());

                    JSONArray subcommands = new JSONArray();
                    cmdPremise.register(builder).getCommands().forEach(cmd -> {
                        System.out.println(cmd.toString());
                        manager.command(cmd);
                        JSONObject subcommand = new JSONObject();
                        subcommand.put("meta", cmd.getCommandMeta().getAllValues());
                        subcommand.put("senderType", cmd.getSenderType().isPresent() ?
                            cmd.getSenderType().get().getName() : null);
                        subcommand.put("toString", cmd.toString());

                        final Iterator<CommandComponent<CommandSender>> iterator = cmd.getComponents().iterator();
                        JSONArray args = new JSONArray();
                        cmd.getArguments().forEach(arg -> {
                            JSONObject obj = new JSONObject();
                            obj.put("name", arg.getName());
                            if (arg instanceof StaticArgument) {
                                obj.put("alias", ((StaticArgument<?>) arg).getAlternativeAliases());
                            }
                            obj.put("isRequired", arg.isRequired());
                            obj.put("defaultValue", arg.getDefaultValue());
                            obj.put("defaultDescription", arg.getDefaultDescription());
                            obj.put("class", arg.getClass().getName());

                            if (iterator.hasNext()) {
                                final CommandComponent<CommandSender> component = iterator.next();
                                if (!component.getArgumentDescription().isEmpty()) {
                                    obj.put("description", component.getArgumentDescription().getDescription());
                                }
                            }
                            args.put(obj);
                        });
                        subcommand.put("arguments", args);
                        subcommands.put(subcommand);
                    });

                    manager.command(builder
                        .literal("help")
                        .handler(context -> minecraftHelp.queryCommands(cmdPremise.details().getName(), context.getSender())));

                    JSONObject details = new JSONObject();
                    details.put("class", instance.getClass().getName());
                    details.put("name", commandName);
                    details.put("command", cmdPremise.details().getName());
                    details.put("description", cmdPremise.details().getDescription());
                    details.put("alias", cmdPremise.details().getAliases());
                    details.put("subcommands", subcommands);
                    commands.put(details);

                    getLogger().info(String.format("%s registered", commandName));
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    getLogger().warning(String.format("%s register failed", commandName));
                    e.printStackTrace();
                }
            }
            MyMaidData.putGetDocsData("commands", commands);
        } catch (ClassNotFoundException | IOException e) {
            getLogger().warning("registerCommand failed");
            e.printStackTrace();
        }
    }

    public static void registerDiscordEvent(JDABuilder d) {
        getJavaPlugin().getLogger().info("----- registerDiscordEvent -----");
        try {
            ClassFinder classFinder = new ClassFinder(getMain().getClassLoader());
            for (Class<?> clazz : classFinder.findClasses("com.jaoafa.mymaid4.discordEvent")) {
                if (!clazz.getName().startsWith("com.jaoafa.mymaid4.discordEvent.DiscordEvent_")) {
                    continue;
                }
                if (clazz.getEnclosingClass() != null) {
                    continue;
                }
                if (clazz.getName().contains("$")) {
                    continue;
                }
                String name = clazz.getName().substring("com.jaoafa.mymaid4.discordEvent.DiscordEvent_".length())
                    .toLowerCase();
                try {
                    Constructor<?> construct = clazz.getConstructor();
                    Object instance = construct.newInstance();

                    d.addEventListeners(instance);
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    getJavaPlugin().getLogger().warning(String.format("%s register failed", name));
                    e.printStackTrace();
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            getJavaPlugin().getLogger().warning("registerCommand failed");
            e.printStackTrace();
        }
    }

    private void scheduleTasks() {
        new MyMaidServer().runTaskAsynchronously(this);
        new Task_Pigeon().runTaskTimerAsynchronously(this, 200L, 12000L); // 10秒後から10分毎
        new Task_TabList().runTaskTimerAsynchronously(this, 200L, 1200L); // 10秒後から1分毎
    }

    public static Main getMain() {
        return Main;
    }

    public static JavaPlugin getJavaPlugin() {
        return Main;
    }

    public static Logger getMyMaidLogger() {
        return Main.getLogger();
    }

    public static MyMaidConfig getMyMaidConfig() {
        return config;
    }
}
