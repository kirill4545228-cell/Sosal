package ru.sosal;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class Msg {
    private static final Component PREFIX = Component.text("[SOSAL] ", NamedTextColor.AQUA, TextDecoration.BOLD);

    private Msg() {}

    public static void info(Audience to, String text) {
        to.sendMessage(PREFIX.append(Component.text(text, NamedTextColor.GREEN).decoration(TextDecoration.BOLD, false)));
    }

    public static void error(Audience to, String text) {
        to.sendMessage(PREFIX.append(Component.text(text, NamedTextColor.RED).decoration(TextDecoration.BOLD, false)));
    }

    public static void plain(Audience to, String text, NamedTextColor color) {
        to.sendMessage(Component.text(text, color));
    }

    public static void bar(Audience to, String text, NamedTextColor color) {
        to.sendActionBar(Component.text(text, color));
    }
}
