package ru.sosal;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class SosalPlugin extends JavaPlugin {
    private FlightManager flight;

    @Override
    public void onEnable() {
        Items items = new Items(this);
        flight = new FlightManager(items);
        Actions actions = new Actions(this, items, flight);
        MenuManager menu = new MenuManager(items, actions);

        getServer().getPluginManager().registerEvents(menu, this);
        getServer().getPluginManager().registerEvents(new AbilityListener(items, actions, flight), this);

        SosalCommand cmd = new SosalCommand(actions, menu);
        PluginCommand pc = getCommand("sosal");
        if (pc != null) {
            pc.setExecutor(cmd);
            pc.setTabCompleter(cmd);
        }

        getServer().getScheduler().runTaskTimer(this, flight::tick, 5L, 3L);
        getLogger().info("SosalPlugin включён. Способностей: " + Ability.values().length);
    }

    @Override
    public void onDisable() {
        if (flight != null) {
            flight.releaseAll();
        }
    }
}
