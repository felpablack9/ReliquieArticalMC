package it.relicevents.events.types;

import it.relicevents.RelicEventsPlugin;
import it.relicevents.events.RelicEvent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class BossEvent implements RelicEvent {

    private final RelicEventsPlugin plugin;
    private Location center;
    private boolean active;
    private LivingEntity boss;
    private final List<LivingEntity> minions;

    private static final String[] BOSS_NAMES = {
            "Signore delle Ombre",
            "Custode Antico",
            "Il Divoratore",
            "Campione Maledetto",
            "Re dei Non-Morti"
    };

    public BossEvent(RelicEventsPlugin plugin) {
        this.plugin = plugin;
        this.active = false;
        this.minions = new ArrayList<>();
    }

    @Override
    public String getId() {
        return "boss";
    }

    @Override
    public String getDisplayName() {
        return "Boss Leggendario";
    }

    @Override
    public void start(Location center) {
        this.center = center;
        this.active = true;

        spawnBoss(center);
        spawnMinions(center);

        // Dramatic effects
        center.getWorld().playSound(center, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.5f);
        center.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, center, 5);
        center.getWorld().strikeLightningEffect(center);

        String prefix = plugin.getConfig().getString("messages.prefix", "&6[RelicEvents] &r");
        String msg = plugin.getConfig().getString("messages.boss-spawn",
                "&4&l☠ BOSS! &c{boss} &4è apparso a X:{x} Z:{z}!");
        msg = msg.replace("{boss}", boss.getCustomName())
                .replace("{x}", String.valueOf(center.getBlockX()))
                .replace("{z}", String.valueOf(center.getBlockZ()));
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));

        // Periodic lightning effects around boss
        Bukkit.getScheduler().runTaskTimer(plugin, (task) -> {
            if (!active || boss == null || boss.isDead()) {
                task.cancel();
                return;
            }
            Location bossLoc = boss.getLocation();
            bossLoc.getWorld().strikeLightningEffect(
                    bossLoc.clone().add((Math.random() - 0.5) * 10, 0, (Math.random() - 0.5) * 10));
            bossLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, bossLoc.add(0, 1, 0), 20, 1, 1, 1);
        }, 100L, 100L);
    }

    private void spawnBoss(Location center) {
        List<String> bossTypeNames = plugin.getConfig().getStringList("events.boss.boss-types");
        EntityType bossType = EntityType.WITHER_SKELETON;

        if (!bossTypeNames.isEmpty()) {
            String typeName = bossTypeNames.get((int) (Math.random() * bossTypeNames.size()));
            try {
                bossType = EntityType.valueOf(typeName);
            } catch (IllegalArgumentException ignored) {
            }
        }

        boss = (LivingEntity) center.getWorld().spawnEntity(center.clone().add(0, 1, 0), bossType);

        String bossName = BOSS_NAMES[(int) (Math.random() * BOSS_NAMES.length)];
        boss.setCustomName(ChatColor.DARK_RED + "⚔ " + bossName + " ⚔");
        boss.setCustomNameVisible(true);

        double healthMultiplier = plugin.getConfig().getDouble("events.boss.boss-health-multiplier", 5.0);
        double maxHealth = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * healthMultiplier;
        boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        boss.setHealth(maxHealth);

        double damageMultiplier = plugin.getConfig().getDouble("events.boss.boss-damage-multiplier", 2.0);
        if (boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            double baseDamage = boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue();
            boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(baseDamage * damageMultiplier);
        }

        // Equip boss
        boss.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
        boss.getEquipment().setHelmet(new ItemStack(Material.NETHERITE_HELMET));
        boss.getEquipment().setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
        boss.getEquipment().setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
        boss.getEquipment().setBoots(new ItemStack(Material.NETHERITE_BOOTS));

        // No drops from equipment
        boss.getEquipment().setItemInMainHandDropChance(0f);
        boss.getEquipment().setHelmetDropChance(0f);
        boss.getEquipment().setChestplateDropChance(0f);
        boss.getEquipment().setLeggingsDropChance(0f);
        boss.getEquipment().setBootsDropChance(0f);

        // Mark as relic boss
        boss.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "relic_boss"),
                PersistentDataType.STRING, "legendary_boss");

        boss.setRemoveWhenFarAway(false);
    }

    private void spawnMinions(Location center) {
        EntityType[] minionTypes = {EntityType.ZOMBIE, EntityType.SKELETON, EntityType.PILLAGER};

        for (int i = 0; i < 8; i++) {
            double offsetX = (Math.random() - 0.5) * 16;
            double offsetZ = (Math.random() - 0.5) * 16;
            Location spawnLoc = center.clone().add(offsetX, 1, offsetZ);

            EntityType type = minionTypes[(int) (Math.random() * minionTypes.length)];
            LivingEntity minion = (LivingEntity) center.getWorld().spawnEntity(spawnLoc, type);
            minion.setCustomName(ChatColor.RED + "Servitore");
            minion.setCustomNameVisible(true);

            minions.add(minion);
        }
    }

    @Override
    public void cleanup() {
        if (boss != null && !boss.isDead()) {
            boss.remove();
        }
        for (LivingEntity minion : minions) {
            if (minion != null && !minion.isDead()) {
                minion.remove();
            }
        }
        minions.clear();
        active = false;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public Location getLocation() {
        return center;
    }

    public LivingEntity getBoss() {
        return boss;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
