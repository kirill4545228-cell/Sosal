package ru.sosal;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public final class Items {
    public static final int JUMP_USES = 3;

    private final NamespacedKey abilityKey;
    private final NamespacedKey usesKey;

    public Items(Plugin plugin) {
        this.abilityKey = new NamespacedKey(plugin, "ability");
        this.usesKey = new NamespacedKey(plugin, "uses");
    }

    public static Component text(String s, NamedTextColor color) {
        return Component.text(s, color).decoration(TextDecoration.ITALIC, false);
    }

    private List<Component> buildLore(Ability a, Integer uses) {
        List<Component> lore = new ArrayList<>();
        for (String line : a.lore()) {
            lore.add(text(line, NamedTextColor.GRAY));
        }
        if (uses != null) {
            lore.add(Component.empty());
            lore.add(text("Осталось прыжков: " + uses, NamedTextColor.GOLD));
        }
        return lore;
    }

    /** Рабочий предмет способности (с меткой в PDC). */
    public ItemStack create(Ability a, int amount) {
        ItemStack stack = new ItemStack(a.icon(), amount);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(text(a.title(), a.color()));
        meta.lore(buildLore(a, null));
        meta.setEnchantmentGlintOverride(true);
        meta.getPersistentDataContainer().set(abilityKey, PersistentDataType.STRING, a.id());
        stack.setItemMeta(meta);
        return stack;
    }

    /** Палочка двойного прыжка с 3 использованиями. */
    public ItemStack createJumpStick() {
        ItemStack stack = new ItemStack(Ability.DOUBLE_JUMP.icon(), 1);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(text(Ability.DOUBLE_JUMP.title(), Ability.DOUBLE_JUMP.color()));
        meta.lore(buildLore(Ability.DOUBLE_JUMP, JUMP_USES));
        meta.setEnchantmentGlintOverride(true);
        meta.setMaxStackSize(1);
        meta.getPersistentDataContainer().set(abilityKey, PersistentDataType.STRING, Ability.DOUBLE_JUMP.id());
        meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, JUMP_USES);
        stack.setItemMeta(meta);
        return stack;
    }

    /** Иконка для меню (без метки — использовать нельзя). */
    public ItemStack icon(Ability a) {
        ItemStack stack = new ItemStack(a.icon(), 1);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(text(a.title(), a.color()));
        List<Component> lore = buildLore(a, null);
        lore.add(Component.empty());
        if (a.kind() == Ability.Kind.INSTANT) {
            lore.add(text("▶ ЛКМ — применить", NamedTextColor.YELLOW));
        } else {
            lore.add(text("▶ ЛКМ — получить предмет", NamedTextColor.YELLOW));
        }
        meta.lore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    public Ability read(ItemStack stack) {
        if (stack == null || stack.getType().isAir() || !stack.hasItemMeta()) {
            return null;
        }
        String id = stack.getItemMeta().getPersistentDataContainer().get(abilityKey, PersistentDataType.STRING);
        return id == null ? null : Ability.byId(id);
    }

    public int getUses(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return 0;
        }
        Integer v = stack.getItemMeta().getPersistentDataContainer().get(usesKey, PersistentDataType.INTEGER);
        return v == null ? 0 : v;
    }

    public void setUses(ItemStack stack, int uses) {
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, uses);
        meta.lore(buildLore(Ability.DOUBLE_JUMP, uses));
        stack.setItemMeta(meta);
    }
}
