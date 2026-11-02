package ru.sosal;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Actions {
    private final SosalPlugin plugin;
    private final Items items;
    private final FlightManager flight;
    private final Map<UUID, Location> deaths = new HashMap<>();
    private final NamespacedKey noGriefKey;

    public Actions(SosalPlugin plugin, Items items, FlightManager flight) {
        this.plugin = plugin;
        this.items = items;
        this.flight = flight;
        this.noGriefKey = new NamespacedKey(plugin, "no_grief");
    }

    public NamespacedKey noGriefKey() {
        return noGriefKey;
    }

    public void recordDeath(Player p) {
        deaths.put(p.getUniqueId(), p.getLocation().clone());
    }

    /** Выдать предмет способности (или палочку) игроку. */
    public void give(Player p, Ability a) {
        ItemStack stack = (a == Ability.DOUBLE_JUMP) ? items.createJumpStick() : items.create(a, a.amount());
        for (ItemStack left : p.getInventory().addItem(stack).values()) {
            p.getWorld().dropItemNaturally(p.getLocation(), left);
        }
    }

    // ------------------------------------------------------------------
    // Мгновенные способности
    // ------------------------------------------------------------------

    public void instant(Player p, Ability a) {
        switch (a) {
            case SPEED -> effect(p, PotionEffectType.SPEED, 90, 1, "Скорость II на 90 секунд");
            case JUMP -> effect(p, PotionEffectType.JUMP_BOOST, 90, 2, "Прыгучесть III на 90 секунд");
            case NIGHT_VISION -> effect(p, PotionEffectType.NIGHT_VISION, 300, 0, "Ночное зрение на 5 минут");
            case INVISIBILITY -> effect(p, PotionEffectType.INVISIBILITY, 60, 0, "Невидимость на 60 секунд");
            case FIRE_RESIST -> effect(p, PotionEffectType.FIRE_RESISTANCE, 300, 0, "Огнестойкость на 5 минут");
            case STRENGTH -> effect(p, PotionEffectType.STRENGTH, 60, 1, "Сила II на 60 секунд");
            case WATER_BREATH -> effect(p, PotionEffectType.WATER_BREATHING, 300, 0, "Подводное дыхание на 5 минут");
            case HASTE -> effect(p, PotionEffectType.HASTE, 120, 1, "Спешка II на 2 минуты");
            case PARACHUTE -> effect(p, PotionEffectType.SLOW_FALLING, 60, 0, "Парашют на 60 секунд");
            case SHIELD -> effect(p, PotionEffectType.RESISTANCE, 20, 2, "Щит: сопротивление III на 20 секунд");
            case HEAL -> heal(p);
            case FLY -> {
                flight.grant(p, 60);
                Msg.info(p, "Полёт на 60 секунд! Дважды нажми пробел.");
            }
            case MAGNET -> magnet(p);
            case FIREWORK -> firework(p);
            case SPAWN -> {
                Location s = p.getWorld().getSpawnLocation().add(0.5, 0, 0.5);
                p.teleport(s);
                Msg.info(p, "Ты на спавне.");
            }
            case DEATH_RETURN -> {
                Location d = deaths.get(p.getUniqueId());
                if (d == null || d.getWorld() == null) {
                    Msg.error(p, "Нет сохранённого места смерти.");
                } else {
                    p.teleport(d);
                    Msg.info(p, "Ты вернулся к месту смерти.");
                }
            }
            case GOOD_DAY -> {
                if (!p.hasPermission("sosal.admin")) {
                    Msg.error(p, "Это только для админов (sosal.admin).");
                } else {
                    World w = p.getWorld();
                    w.setStorm(false);
                    w.setThundering(false);
                    w.setTime(1000L);
                    Msg.info(p, "Наступил ясный день.");
                }
            }
            default -> { }
        }
    }

    private void effect(Player p, PotionEffectType type, int seconds, int amplifier, String msg) {
        p.addPotionEffect(new PotionEffect(type, seconds * 20, amplifier, false, true, true));
        Msg.info(p, msg);
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);
    }

    private void heal(Player p) {
        AttributeInstance max = p.getAttribute(Attribute.MAX_HEALTH);
        p.setHealth(max != null ? max.getValue() : 20.0);
        p.setFoodLevel(20);
        p.setSaturation(20f);
        p.setFireTicks(0);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, p.getLocation().add(0, 1, 0), 30, 0.4, 0.6, 0.4, 0.2);
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        Msg.info(p, "Ты полностью исцелён.");
    }

    private void magnet(Player p) {
        int count = 0;
        for (Entity e : p.getNearbyEntities(15, 15, 15)) {
            if (e instanceof Item item) {
                item.setPickupDelay(0);
                item.teleport(p.getLocation());
                count++;
            }
        }
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.5f);
        Msg.info(p, "Притянуто предметов: " + count);
    }

    private void firework(Player p) {
        p.getWorld().spawn(p.getLocation(), Firework.class, fw -> {
            FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder()
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .withColor(Color.AQUA, Color.YELLOW)
                    .withFade(Color.RED)
                    .trail(true)
                    .flicker(true)
                    .build());
            meta.setPower(1);
            fw.setFireworkMeta(meta);
        });
        Msg.info(p, "Салют!");
    }

    // ------------------------------------------------------------------
    // Предметные способности. true = заряд потрачен
    // ------------------------------------------------------------------

    public boolean use(Player p, Ability a) {
        return switch (a) {
            case DASH -> dash(p);
            case FIREBALL -> fireball(p);
            case LIGHTNING -> lightning(p);
            case EXPLOSION -> explosion(p);
            case TELEPORT -> teleportEye(p);
            case ROCKET -> rocket(p);
            case SHOCKWAVE -> shockwave(p);
            case ICE -> ice(p);
            case FIRE_RING -> fireRing(p);
            case SMOKE -> smoke(p);
            case WOLVES -> wolves(p);
            default -> false;
        };
    }

    private boolean dash(Player p) {
        World w = p.getWorld();
        Location start = p.getLocation();
        Location aim = start.clone();
        if (p.isOnGround() && aim.getPitch() > 0) {
            aim.setPitch(0);
        }
        Vector dir = aim.getDirection().normalize();

        Location last = null;
        for (double t = 0.25; t <= 5.0001; t += 0.25) {
            Location c = start.clone().add(dir.clone().multiply(t));
            if (!w.getWorldBorder().isInside(c)) break;
            if (!w.isChunkLoaded(c.getBlockX() >> 4, c.getBlockZ() >> 4)) break;
            if (!isFree(c)) break;
            last = c;
        }

        if (last == null || last.distanceSquared(start) < 1.0) {
            Msg.bar(p, "Путь заблокирован!", NamedTextColor.RED);
            return false;
        }

        // след из частиц
        double dist = last.distance(start);
        for (double t = 0; t <= dist; t += 0.4) {
            Location pt = start.clone().add(dir.clone().multiply(t)).add(0, 1, 0);
            w.spawnParticle(Particle.CLOUD, pt, 3, 0.15, 0.2, 0.15, 0.01);
        }

        p.teleport(last);
        p.setFallDistance(0f);
        w.playSound(last, Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1.6f);
        Msg.bar(p, "Рывок!", NamedTextColor.AQUA);
        return true;
    }

    private boolean isFree(Location c) {
        double[] heights = {0.0, 1.0, 1.8};
        for (double dy : heights) {
            Block b = c.clone().add(0, dy, 0).getBlock();
            if (!b.isPassable() || b.getType() == Material.LAVA) {
                return false;
            }
        }
        return true;
    }

    private RayTraceResult ray(Player p, double distance) {
        Location eye = p.getEyeLocation();
        return p.getWorld().rayTraceBlocks(eye, eye.getDirection(), distance, FluidCollisionMode.NEVER, true);
    }

    private boolean fireball(Player p) {
        Vector dir = p.getEyeLocation().getDirection();
        Fireball fb = p.launchProjectile(Fireball.class, dir.clone().multiply(1.2));
        fb.setDirection(dir.clone().multiply(1.0));
        fb.setIsIncendiary(false);
        fb.setYield(1.5f);
        fb.getPersistentDataContainer().set(noGriefKey, PersistentDataType.BYTE, (byte) 1);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);
        return true;
    }

    private boolean lightning(Player p) {
        RayTraceResult r = ray(p, 40);
        Location target;
        if (r != null) {
            target = r.getHitPosition().toLocation(p.getWorld());
        } else {
            target = p.getEyeLocation().add(p.getEyeLocation().getDirection().multiply(40));
        }
        p.getWorld().strikeLightning(target);
        return true;
    }

    private boolean explosion(Player p) {
        RayTraceResult r = ray(p, 30);
        if (r == null) {
            Msg.bar(p, "Цель слишком далеко!", NamedTextColor.RED);
            return false;
        }
        Location target = r.getHitPosition().toLocation(p.getWorld());
        p.getWorld().createExplosion(target, 2.5f, false, false);
        return true;
    }

    private boolean teleportEye(Player p) {
        RayTraceResult r = ray(p, 30);
        if (r == null) {
            Msg.bar(p, "Цель слишком далеко!", NamedTextColor.RED);
            return false;
        }
        Location t = r.getHitPosition().toLocation(p.getWorld());
        if (r.getHitBlockFace() != null) {
            t.add(r.getHitBlockFace().getDirection().multiply(0.5));
        }
        if (!isFree(t)) {
            t.add(0, 1, 0);
            if (!isFree(t)) {
                Msg.bar(p, "Туда нельзя телепортироваться!", NamedTextColor.RED);
                return false;
            }
        }
        t.setYaw(p.getLocation().getYaw());
        t.setPitch(p.getLocation().getPitch());
        p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation().add(0, 1, 0), 40, 0.3, 0.6, 0.3, 0.5);
        p.teleport(t);
        p.setFallDistance(0f);
        p.getWorld().spawnParticle(Particle.PORTAL, t.clone().add(0, 1, 0), 40, 0.3, 0.6, 0.3, 0.5);
        p.getWorld().playSound(t, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        return true;
    }

    private boolean rocket(Player p) {
        p.setVelocity(new Vector(0, 1.6, 0));
        p.setFallDistance(0f);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 160, 0, false, false, false));
        p.getWorld().spawnParticle(Particle.FLAME, p.getLocation(), 40, 0.3, 0.1, 0.3, 0.05);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f);
        return true;
    }

    private boolean shockwave(Player p) {
        Location c = p.getLocation();
        for (Entity e : p.getNearbyEntities(6, 3, 6)) {
            if (e instanceof LivingEntity) {
                Vector push = e.getLocation().toVector().subtract(c.toVector());
                if (push.lengthSquared() < 1e-4) {
                    push = new Vector(1, 0, 0);
                }
                push.setY(0).normalize().multiply(1.6).setY(0.6);
                e.setVelocity(push);
            }
        }
        p.getWorld().spawnParticle(Particle.EXPLOSION, c.clone().add(0, 0.5, 0), 3, 1.5, 0.2, 1.5, 0);
        p.getWorld().playSound(c, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.2f);
        return true;
    }

    private boolean ice(Player p) {
        Location c = p.getLocation();
        for (Entity e : p.getNearbyEntities(6, 4, 6)) {
            if (e instanceof LivingEntity le) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 3, false, true, true));
                le.setFreezeTicks(le.getMaxFreezeTicks());
            }
        }
        p.getWorld().spawnParticle(Particle.SNOWFLAKE, c.clone().add(0, 1, 0), 120, 3, 1, 3, 0.02);
        p.getWorld().playSound(c, Sound.ENTITY_PLAYER_HURT_FREEZE, 1f, 1f);
        return true;
    }

    private boolean fireRing(Player p) {
        Location c = p.getLocation();
        for (Entity e : p.getNearbyEntities(5, 3, 5)) {
            if (e instanceof LivingEntity le) {
                le.setFireTicks(100);
            }
        }
        for (int i = 0; i < 40; i++) {
            double ang = Math.PI * 2 * i / 40.0;
            Location pt = c.clone().add(Math.cos(ang) * 5, 0.3, Math.sin(ang) * 5);
            p.getWorld().spawnParticle(Particle.FLAME, pt, 3, 0.1, 0.2, 0.1, 0.01);
        }
        p.getWorld().playSound(c, Sound.ITEM_FIRECHARGE_USE, 1f, 1f);
        return true;
    }

    private boolean smoke(Player p) {
        Location c = p.getLocation();
        for (Entity e : p.getNearbyEntities(7, 4, 7)) {
            if (e instanceof LivingEntity le) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, false, true, true));
            }
        }
        p.getWorld().spawnParticle(Particle.LARGE_SMOKE, c.clone().add(0, 1, 0), 150, 3, 1.2, 3, 0.02);
        p.getWorld().playSound(c, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1f, 0.8f);
        return true;
    }

    private boolean wolves(Player p) {
        for (int i = 0; i < 3; i++) {
            Wolf wolf = p.getWorld().spawn(p.getLocation(), Wolf.class);
            wolf.setTamed(true);
            wolf.setOwner(p);
            wolf.customName(Component.text("Волк " + p.getName(), NamedTextColor.WHITE));
            wolf.setCustomNameVisible(true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (wolf.isValid()) {
                    wolf.getWorld().spawnParticle(Particle.CLOUD, wolf.getLocation(), 15, 0.3, 0.3, 0.3, 0.02);
                    wolf.remove();
                }
            }, 600L);
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        return true;
    }
}
