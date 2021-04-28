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
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import com.jaoafa.mymaid4.httpServer.MyMaidServer;
import com.jaoafa.mymaid4.lib.*;
import com.jaoafa.mymaid4.tasks.Task_Pigeon;
import com.jaoafa.mymaid4.tasks.Task_TabList;
import net.dv8tion.jda.api.JDABuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class Main extends JavaPlugin {
    private static Main Main = null;
    private static MyMaidConfig config = null;

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
    }

    @Override
    public void onDisable() {
        if(config.getJDA() != null) {
            config.getJDA().getEventManager().getRegisteredListeners()
                .forEach(listener -> config.getJDA().getEventManager().unregister(listener));
            config.getJDA().shutdownNow();
        }
        MyMaidServer.stopServer();
    }

    private void registerCommand() {
        getLogger().info("----- registerCommand -----");
        final PaperCommandManager<CommandSender> manager;
        try {
            manager = new PaperCommandManager<>(this, CommandExecutionCoordinator.SimpleCoordinator.simpleCoordinator(),
                Function.identity(), Function.identity());
            manager.registerBrigadier();
        } catch (Exception e) {
            getLogger().warning("コマンドの登録に失敗しました。PaperCommandManagerを取得できません。");
            e.printStackTrace();
            return;
        }

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

    private void scheduleTasks(){
        new MyMaidServer().runTaskAsynchronously(this);
        new Task_Pigeon().runTaskTimerAsynchronously(this, 200L, 12000L); // 10秒後から10分毎
        new Task_TabList().runTaskTimerAsynchronously(this, 200L, 1200L); // 10秒後から1分毎
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
