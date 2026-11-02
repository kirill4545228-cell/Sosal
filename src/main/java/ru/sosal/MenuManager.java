package ru.sosal;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class MenuManager implements Listener {
    private static final int SIZE = 54;
    private static final int FIRST_SLOT = 9;
    private static final int LAST_SLOT = 44;
    private static final int INFO_SLOT = 4;
    private static final int CLOSE_SLOT = 49;

    public static final class MenuHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private final Items items;
    private final Actions actions;
    private final Ability[] slotMap = new Ability[SIZE];

    public MenuManager(Items items, Actions actions) {
        this.items = items;
        this.actions = actions;
        int slot = FIRST_SLOT;
        for (Ability a : Ability.values()) {
            if (slot > LAST_SLOT) break;
            slotMap[slot++] = a;
        }
    }

    public void open(Player p) {
        MenuHolder holder = new MenuHolder();
        Inventory inv = Bukkit.createInventory(holder, SIZE,
                Component.text("Меню SOSAL", NamedTextColor.DARK_AQUA));
        holder.inventory = inv;

        ItemStack pane = named(Material.BLACK_STAINED_GLASS_PANE, Items.text(" ", NamedTextColor.WHITE));
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, pane);
        }

        for (int i = FIRST_SLOT; i <= LAST_SLOT; i++) {
            if (slotMap[i] != null) {
                inv.setItem(i, items.icon(slotMap[i]));
            } else {
                inv.setItem(i, null);
            }
        }

        ItemStack info = named(Material.BOOK, Items.text("Все функции SOSAL", NamedTextColor.AQUA));
        ItemMeta im = info.getItemMeta();
        im.lore(java.util.List.of(
                Items.text("ЛКМ по иконке:", NamedTextColor.GRAY),
                Items.text("• предметы — выдаются в инвентарь", NamedTextColor.GRAY),
                Items.text("• способности — применяются сразу", NamedTextColor.GRAY)));
        info.setItemMeta(im);
        inv.setItem(INFO_SLOT, info);

        inv.setItem(CLOSE_SLOT, named(Material.BARRIER, Items.text("Закрыть", NamedTextColor.RED)));

        p.openInventory(inv);
    }

    private ItemStack named(Material m, Component name) {
        ItemStack s = new ItemStack(m);
        ItemMeta meta = s.getItemMeta();
        meta.displayName(name);
        s.setItemMeta(meta);
        return s;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof MenuHolder)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getInventory()) return;
        if (!e.getClick().isLeftClick()) return;

        int slot = e.getSlot();
        if (slot == CLOSE_SLOT) {
            p.closeInventory();
            return;
        }
        if (slot < 0 || slot >= SIZE) return;
        Ability a = slotMap[slot];
        if (a == null) return;

        if (a.kind() == Ability.Kind.INSTANT) {
            p.closeInventory();
            actions.instant(p, a);
        } else {
            actions.give(p, a);
            p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.7f, 1f);
            Msg.bar(p, "Получено: " + a.title(), NamedTextColor.GREEN);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof MenuHolder) {
            e.setCancelled(true);
        }
    }
}
