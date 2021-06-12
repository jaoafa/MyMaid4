package com.jaoafa.mymaid4;

import com.jaoafa.mymaid4.lib.ClassFinder;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.EventPremise;
import org.bukkit.event.Listener;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.fail;

public class MainTest {
    @Test
    public void CommandTest() {
        System.out.println("----- registerCommand -----");

        try {
            ClassFinder classFinder = new ClassFinder();
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

                    if (!(instance instanceof CommandPremise)) {
                        System.out.println("[" + commandName + "] This command class does not implement CommandPremise.");
                        fail();
                        continue;
                    }

                    System.out.println("[" + commandName + "] registrable.");
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    System.out.println("[" + commandName + "] Register failed.");
                    e.printStackTrace();
                    fail();
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            System.out.println("Command register failed.");
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void EventTest() {
        System.out.println("----- registerEvent -----");
        try {
            ClassFinder classFinder = new ClassFinder();
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

                    if (!(instance instanceof EventPremise)) {
                        System.out.println("[" + name + "] This command class does not implement EventPremise.");
                        fail();
                        continue;
                    }

                    if (!(instance instanceof Listener)) {
                        System.out.println("[" + name + "] This event class does not implement Listener.");
                        fail();
                        return;
                    }

                    try {
                        Listener listener = (Listener) instance;
                        System.out.println("[" + name + "] registrable. (" + listener + ")");
                    } catch (ClassCastException e) {
                        System.out.printf("%s: ClassCastException%n", clazz.getSimpleName());
                        fail();
                    }
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    System.out.printf("%s register failed%n", name);
                    e.printStackTrace();
                    fail();
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            System.out.println("Event register failed.");
            e.printStackTrace();
            fail();
        }
    }
}
