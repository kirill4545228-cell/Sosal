package ru.sosal;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Управляет allowFlight: включает его, пока игрок держит палочку
 * (нужно для детекта двойного прыжка) или пока активен временный полёт.
 */
public final class FlightManager {
    private final Items items;
    private final Set<UUID> enabledByUs = new HashSet<>();
    private final Map<UUID, Long> timed = new HashMap<>();

    public FlightManager(Items items) {
        this.items = items;
    }

    public void grant(Player p, int seconds) {
        timed.put(p.getUniqueId(), System.currentTimeMillis() + seconds * 1000L);
    }

    public boolean hasTimed(Player p) {
        Long until = timed.get(p.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    public void tick() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            update(p);
        }
    }

    private void update(Player p) {
        UUID id = p.getUniqueId();
        long now = System.currentTimeMillis();

        Long until = timed.get(id);
        if (until != null && until <= now) {
            timed.remove(id);
            Msg.bar(p, "Полёт закончился", NamedTextColor.RED);
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0, false, true, true));
        }

        GameMode gm = p.getGameMode();
        if (gm != GameMode.SURVIVAL && gm != GameMode.ADVENTURE) {
            enabledByUs.remove(id);
            return;
        }

        boolean want = hasTimed(p)
                || items.read(p.getInventory().getItemInMainHand()) == Ability.DOUBLE_JUMP;

        if (want) {
            if (!p.getAllowFlight()) {
                p.setAllowFlight(true);
                enabledByUs.add(id);
            }
        } else if (enabledByUs.remove(id)) {
            p.setFlying(false);
            p.setAllowFlight(false);
        }
    }

    public void release(Player p) {
        timed.remove(p.getUniqueId());
        if (enabledByUs.remove(p.getUniqueId())) {
            p.setFlying(false);
            p.setAllowFlight(false);
        }
    }

    public void releaseAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            release(p);
        }
    }
}
