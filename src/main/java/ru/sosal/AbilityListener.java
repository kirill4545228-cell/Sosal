package ru.sosal;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AbilityListener implements Listener {
    private final Items items;
    private final Actions actions;
    private final FlightManager flight;
    private final Map<UUID, Long> lastUse = new HashMap<>();

    public AbilityListener(Items items, Actions actions, FlightManager flight) {
        this.items = items;
        this.actions = actions;
        this.flight = flight;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Action act = e.getAction();
        if (act == Action.PHYSICAL) return;

        Ability a = items.read(e.getItem());
        if (a == null || a.kind() == Ability.Kind.INSTANT) return;
        if (a.kind() == Ability.Kind.SPECIAL) return; // палочка работает через двойной прыжок

        e.setCancelled(true);

        boolean right = act == Action.RIGHT_CLICK_AIR || act == Action.RIGHT_CLICK_BLOCK;
        if (a != Ability.DASH && !right) return;

        EquipmentSlot hand = e.getHand();
        if (hand == null) return;

        Player p = e.getPlayer();
        long now = System.currentTimeMillis();
        Long last = lastUse.get(p.getUniqueId());
        if (last != null && now - last < 250) return;
        lastUse.put(p.getUniqueId(), now);

        if (actions.use(p, a)) {
            consume(p, hand);
        }
    }

    private void consume(Player p, EquipmentSlot hand) {
        ItemStack s = p.getInventory().getItem(hand);
        if (s == null || s.getType().isAir()) return;
        if (s.getAmount() <= 1) {
            p.getInventory().setItem(hand, null);
        } else {
            s.setAmount(s.getAmount() - 1);
            p.getInventory().setItem(hand, s);
        }
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent e) {
        if (!e.isFlying()) return;
        Player p = e.getPlayer();

        GameMode gm = p.getGameMode();
        if (gm != GameMode.SURVIVAL && gm != GameMode.ADVENTURE) return;
        if (flight.hasTimed(p)) return; // настоящий полёт из меню

        ItemStack hand = p.getInventory().getItemInMainHand();
        if (items.read(hand) != Ability.DOUBLE_JUMP) return;

        e.setCancelled(true);
        p.setFlying(false);

        if (p.isOnGround()) return; // прыжок только в воздухе

        int uses = items.getUses(hand) - 1;

        Vector dir = p.getLocation().getDirection();
        Vector horizontal = new Vector(dir.getX(), 0, dir.getZ());
        if (horizontal.lengthSquared() > 1e-4) {
            horizontal.normalize().multiply(0.5);
        } else {
            horizontal = new Vector();
        }
        p.setVelocity(new Vector(horizontal.getX(), 0.85, horizontal.getZ()));
        p.setFallDistance(0f);
        p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation(), 15, 0.3, 0.05, 0.3, 0.02);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1f, 1.3f);

        if (uses <= 0) {
            p.getInventory().setItemInMainHand(null);
            Msg.bar(p, "Двойной прыжок! Палочка рассыпалась", NamedTextColor.RED);
        } else {
            items.setUses(hand, uses);
            p.getInventory().setItemInMainHand(hand);
            Msg.bar(p, "Двойной прыжок! Осталось: " + uses, NamedTextColor.GOLD);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (items.read(e.getItemInHand()) != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        actions.recordDeath(e.getEntity());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        flight.release(e.getPlayer());
        lastUse.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if (e.getEntity().getPersistentDataContainer().has(actions.noGriefKey(), PersistentDataType.BYTE)) {
            e.blockList().clear();
        }
    }
}
