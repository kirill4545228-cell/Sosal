package ru.sosal;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Ability {
    // ---- Основные ----
    DASH("dash", "Пёрышко-рывок", Material.FEATHER, NamedTextColor.AQUA, Kind.ITEM, 1,
            "ЛКМ или ПКМ — рывок на 5 блоков вперёд.",
            "После рывка пёрышко исчезает."),
    DOUBLE_JUMP("double_jump", "Палочка двойного прыжка", Material.STICK, NamedTextColor.GOLD, Kind.SPECIAL, 1,
            "Держи в руке и нажми пробел",
            "второй раз в воздухе — двойной прыжок.",
            "Пропадает после 3 прыжков."),

    // ---- Мгновенные способности (применяются из меню) ----
    SPEED("speed", "Скорость", Material.SUGAR, NamedTextColor.WHITE, Kind.INSTANT, 1,
            "Скорость II на 90 секунд."),
    JUMP("jump", "Прыгучесть", Material.RABBIT_FOOT, NamedTextColor.GREEN, Kind.INSTANT, 1,
            "Прыгучесть III на 90 секунд."),
    NIGHT_VISION("night_vision", "Ночное зрение", Material.GOLDEN_CARROT, NamedTextColor.YELLOW, Kind.INSTANT, 1,
            "Ночное зрение на 5 минут."),
    HEAL("heal", "Исцеление", Material.GLISTERING_MELON_SLICE, NamedTextColor.RED, Kind.INSTANT, 1,
            "Полностью восстанавливает",
            "здоровье и сытость."),
    INVISIBILITY("invisibility", "Невидимость", Material.FERMENTED_SPIDER_EYE, NamedTextColor.GRAY, Kind.INSTANT, 1,
            "Невидимость на 60 секунд."),
    FIRE_RESIST("fire_resist", "Огнестойкость", Material.MAGMA_CREAM, NamedTextColor.GOLD, Kind.INSTANT, 1,
            "Огнестойкость на 5 минут."),
    STRENGTH("strength", "Сила", Material.IRON_SWORD, NamedTextColor.DARK_RED, Kind.INSTANT, 1,
            "Сила II на 60 секунд."),
    WATER_BREATH("water_breath", "Подводное дыхание", Material.PUFFERFISH, NamedTextColor.AQUA, Kind.INSTANT, 1,
            "Дыхание под водой на 5 минут."),
    HASTE("haste", "Шахтёрская спешка", Material.GOLDEN_PICKAXE, NamedTextColor.YELLOW, Kind.INSTANT, 1,
            "Спешка II на 2 минуты."),
    PARACHUTE("parachute", "Парашют", Material.PHANTOM_MEMBRANE, NamedTextColor.WHITE, Kind.INSTANT, 1,
            "Медленное падение на 60 секунд."),
    SHIELD("shield", "Щит", Material.SHIELD, NamedTextColor.BLUE, Kind.INSTANT, 1,
            "Сопротивление урону III",
            "на 20 секунд."),
    FLY("fly", "Полёт", Material.ELYTRA, NamedTextColor.LIGHT_PURPLE, Kind.INSTANT, 1,
            "Настоящий полёт на 60 секунд."),
    MAGNET("magnet", "Магнит", Material.LODESTONE, NamedTextColor.GRAY, Kind.INSTANT, 1,
            "Притягивает все предметы",
            "в радиусе 15 блоков."),
    FIREWORK("firework", "Салют", Material.FIREWORK_STAR, NamedTextColor.LIGHT_PURPLE, Kind.INSTANT, 1,
            "Запускает красивый салют."),
    SPAWN("spawn", "На спавн", Material.COMPASS, NamedTextColor.GREEN, Kind.INSTANT, 1,
            "Телепорт на спавн мира."),
    DEATH_RETURN("death_return", "К месту смерти", Material.SKELETON_SKULL, NamedTextColor.DARK_GRAY, Kind.INSTANT, 1,
            "Телепорт туда, где ты",
            "умер в последний раз."),
    GOOD_DAY("good_day", "Ясный день", Material.SUNFLOWER, NamedTextColor.YELLOW, Kind.INSTANT, 1,
            "Ясная погода и день в мире.",
            "Только для админов (sosal.admin)."),

    // ---- Предметы (ПКМ) ----
    FIREBALL("fireball", "Огненный шар", Material.FIRE_CHARGE, NamedTextColor.GOLD, Kind.ITEM, 5,
            "ПКМ — запустить огненный шар.",
            "Блоки не ломает."),
    LIGHTNING("lightning", "Молния", Material.LIGHTNING_ROD, NamedTextColor.YELLOW, Kind.ITEM, 5,
            "ПКМ — ударить молнией",
            "туда, куда смотришь (до 40 блоков)."),
    EXPLOSION("explosion", "Взрывной посох", Material.TNT, NamedTextColor.RED, Kind.ITEM, 5,
            "ПКМ — взрыв в точке прицела",
            "(до 30 блоков). Блоки не ломает."),
    TELEPORT("teleport", "Телепорт-глаз", Material.ENDER_EYE, NamedTextColor.DARK_PURPLE, Kind.ITEM, 5,
            "ПКМ — телепорт в точку прицела",
            "(до 30 блоков)."),
    ROCKET("rocket", "Ракетный прыжок", Material.FIREWORK_ROCKET, NamedTextColor.RED, Kind.ITEM, 5,
            "ПКМ — мощный прыжок вверх",
            "без урона от падения."),
    SHOCKWAVE("shockwave", "Ударная волна", Material.AMETHYST_SHARD, NamedTextColor.LIGHT_PURPLE, Kind.ITEM, 5,
            "ПКМ — отбрасывает всех",
            "существ вокруг (радиус 6)."),
    ICE("ice", "Ледяной посох", Material.BLUE_ICE, NamedTextColor.AQUA, Kind.ITEM, 5,
            "ПКМ — замораживает и замедляет",
            "всех вокруг (радиус 6)."),
    FIRE_RING("fire_ring", "Огненное кольцо", Material.BLAZE_POWDER, NamedTextColor.GOLD, Kind.ITEM, 5,
            "ПКМ — поджигает всех вокруг",
            "(радиус 5), кроме тебя."),
    SMOKE("smoke", "Дымовая шашка", Material.GUNPOWDER, NamedTextColor.GRAY, Kind.ITEM, 5,
            "ПКМ — облако дыма, ослепляет",
            "врагов рядом (радиус 7)."),
    WOLVES("wolves", "Призыв волков", Material.BONE, NamedTextColor.WHITE, Kind.ITEM, 3,
            "ПКМ — призывает 3 ручных волков",
            "на 30 секунд.");

    public enum Kind { ITEM, INSTANT, SPECIAL }

    private static final Map<String, Ability> BY_ID = new HashMap<>();

    static {
        for (Ability a : values()) {
            BY_ID.put(a.id, a);
        }
    }

    private final String id;
    private final String title;
    private final Material icon;
    private final NamedTextColor color;
    private final Kind kind;
    private final int amount;
    private final List<String> lore;

    Ability(String id, String title, Material icon, NamedTextColor color, Kind kind, int amount, String... lore) {
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.color = color;
        this.kind = kind;
        this.amount = amount;
        this.lore = List.of(lore);
    }

    public String id() { return id; }
    public String title() { return title; }
    public Material icon() { return icon; }
    public NamedTextColor color() { return color; }
    public Kind kind() { return kind; }
    public int amount() { return amount; }
    public List<String> lore() { return lore; }

    public static Ability byId(String id) {
        return BY_ID.get(id);
    }
}
