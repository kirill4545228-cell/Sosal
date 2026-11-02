package ru.sosal;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SosalCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUB = List.of("menu", "feather", "stick", "help");

    private final Actions actions;
    private final MenuManager menu;

    public SosalCommand(Actions actions, MenuManager menu) {
        this.actions = actions;
        this.menu = menu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Эту команду можно использовать только в игре.");
            return true;
        }

        if (args.length == 0) {
            actions.give(p, Ability.DASH);
            actions.give(p, Ability.DOUBLE_JUMP);
            Msg.info(p, "Ты получил пёрышко-рывок и палочку двойного прыжка!");
            Msg.plain(p, "Все функции: /sosal menu", NamedTextColor.GRAY);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "menu" -> menu.open(p);
            case "feather" -> {
                actions.give(p, Ability.DASH);
                Msg.info(p, "Ты получил пёрышко-рывок.");
            }
            case "stick" -> {
                actions.give(p, Ability.DOUBLE_JUMP);
                Msg.info(p, "Ты получил палочку двойного прыжка.");
            }
            default -> {
                Msg.plain(p, "/sosal — выдать пёрышко и палочку", NamedTextColor.AQUA);
                Msg.plain(p, "/sosal menu — меню со всеми функциями", NamedTextColor.AQUA);
                Msg.plain(p, "/sosal feather — только пёрышко", NamedTextColor.AQUA);
                Msg.plain(p, "/sosal stick — только палочка", NamedTextColor.AQUA);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            for (String s : SUB) {
                if (s.startsWith(prefix)) out.add(s);
            }
        }
        return out;
    }
}
