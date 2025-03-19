package me.mogubea.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.mogubea.attributes.Attributes;
import me.mogubea.entities.CustomEntityType;
import me.mogubea.items.ItemRarity;
import me.mogubea.main.Main;
import me.mogubea.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

public class EntityListener extends EventListener {

    private final NamespacedKey KEY_ENTITY_LEVEL;

    protected EntityListener(Main plugin) {
        super(plugin);

        KEY_ENTITY_LEVEL = new NamespacedKey(plugin, "ENTITY_LEVEL");

        if (!getPlugin().hasProtocolManager()) return;

        getPlugin().getProtocolManager().addPacketListener(new PacketAdapter(getPlugin(), PacketType.Play.Server.SPAWN_ENTITY) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player p = event.getPlayer();
                Entity e = p.getWorld().getEntity(event.getPacket().getUUIDs().read(0));
                if (!(e instanceof Item item)) return;
                if (!item.hasMetadata("rarity")) return;

                String rarityName = ItemRarity.valueOf(item.getMetadata("rarity").get(0).asString()).name();
                Team team = p.getScoreboard().getTeam("itemRarity_" + rarityName);
                if (team != null)
                    team.addEntity(item);
            }
        });
    }

    @EventHandler
    public void onMobInteract(EntityInteractEvent e) {
        // Prevent entity trampling
        if (e.getBlock().getType() == Material.FARMLAND)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent e) {
        Item item = e.getEntity();
        ItemRarity itemRarity = getPlugin().getItemManager().getItemRarity(item.getItemStack());

        // Format all item entity item stacks
        item.setItemStack(getPlugin().getItemManager().formatItemStack(item.getItemStack()));

        switch (itemRarity) {
            case TRASH -> item.setTicksLived(20 * 180); // De-spawn faster
            case COMMON -> item.setTicksLived(20 * 120); // De-spawn slightly faster
            case ADMIN -> item.remove(); // Just don't let these items exist in the world
            default -> {
                item.setInvulnerable(true);

                if (!getPlugin().hasProtocolManager()) return;

                item.setGlowing(true);
                item.setMetadata("rarity", new FixedMetadataValue(getPlugin(), itemRarity.name()));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent e) {
        LivingEntity entity = e.getEntity();
        if (cantBeLevelled(entity.getType())) return;

        // Give the entity a level based on distance from 0,0. Entities >= 5,000 blocks away will be level 2, increasing by 1 for every 1,000 blocks.
        PersistentDataContainer container = entity.getPersistentDataContainer();
        byte level = (byte) Math.max(1, (int) ((Math.abs(e.getLocation().getX()) + Math.abs(e.getLocation().getZ()) - 3000) / 1000));
        if (level <= 1) return;

        container.set(KEY_ENTITY_LEVEL, PersistentDataType.BYTE, level);

        HashMap<Attribute, AttributeModifier> amplificationList = new HashMap<>(1);
        amplificationList.put(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier("level_health_amp", 1 + (double)(level-1) * 0.2, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        amplificationList.put(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier("level_attack_amp", 1 + (double)(level-1) * 0.15, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        amplificationList.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier("level_speed_amp", 1 + (double)(level-1) * 0.02, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        amplificationList.forEach((attribute, attributeModifier) -> {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance == null) {
                entity.registerAttribute(attribute);
                instance = entity.getAttribute(attribute);
            }

            Objects.requireNonNull(instance).addModifier(attributeModifier);
        });

        entity.setHealth(Objects.requireNonNull(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityKill(EntityDeathEvent e) {
        if (cantBeLevelled(e.getEntityType())) return;

        byte level = e.getEntity().getPersistentDataContainer().getOrDefault(KEY_ENTITY_LEVEL, PersistentDataType.BYTE, (byte)1);
        e.setDroppedExp((int) ((double)e.getDroppedExp() * ((double)level * 0.15)));
    }

    private boolean cantBeLevelled(@NotNull EntityType type) {
        return !switch (type) {
            case ZOMBIE, WITHER_SKELETON, SKELETON, CREEPER, ENDERMAN, SPIDER, CAVE_SPIDER -> true;
            default -> false;
        };
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityHeal(EntityRegainHealthEvent e) {
        if (e.getAmount() <= 0) return;
        if (!(e.getEntity() instanceof LivingEntity living)) return;

        CustomEntityType.TEXT_INDICATOR.spawn(living.getLocation()).setText(Component.text("+" + e.getAmount(), TextColor.color(0x33ff77)));
    /*    getPlugin().getServer().getScheduler().runTaskLaterAsynchronously(getPlugin(), () ->
                updateNametag(living,
                        Component.translatable(living.getType().translationKey(), NamedTextColor.GRAY)
                                .append(Component.text("    \u2764" + living.getHealth() + "/" + Objects.requireNonNull(living.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue(), NamedTextColor.RED))), 1L);
    */
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getFinalDamage() <= 0) return;
        if (!(e.getEntity() instanceof LivingEntity living)) return;

        // Do dodge chance
        if (living instanceof Player player) {
            PlayerProfile profile = PlayerProfile.from(player);
            double dodgeChance = profile.getAttributes().getValue(Attributes.DODGE_CHANCE);
            if (Math.random() * 100 <= dodgeChance) {
                CustomEntityType.TEXT_INDICATOR.spawn(living.getLocation()).setText(Component.text("*dodge*", TextColor.color(0xcfcfff)));
                player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 0.3F, 1.3F);
                e.setCancelled(true);
                return;
            }
        }

        CustomEntityType.TEXT_INDICATOR.spawn(living.getLocation()).setText(Component.text(e.getFinalDamage(), TextColor.color(0xdf2233)));
     /*   getPlugin().getServer().getScheduler().runTaskLaterAsynchronously(getPlugin(), () ->
                updateNametag(living,
                Component.translatable(living.getType().translationKey(), NamedTextColor.GRAY)
                .append(Component.text("    \u2764" + living.getHealth() + "/" + Objects.requireNonNull(living.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue(), NamedTextColor.RED))), 1L);
    */
    }

   /* public void updateNametag(LivingEntity entity, Component nametag) {
        WrappedDataWatcher dataWatcher = WrappedDataWatcher.getEntityWatcher(entity).deepClone();
        WrappedDataWatcher.Serializer chatSerializer = WrappedDataWatcher.Registry.getChatComponentSerializer(true);
        WrappedDataWatcher.WrappedDataWatcherObject watcherObject = new WrappedDataWatcher.WrappedDataWatcherObject(2, chatSerializer);
        Optional<Object> optional = Optional.of(WrappedChatComponent.fromJson(nametag.toString()).getHandle());
        dataWatcher.setObject(watcherObject, optional);
        dataWatcher.setObject(3, true);

        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());
        packet.getIntegers().write(0, entity.getEntityId());

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(onlinePlayer, packet);
            } catch (InvocationTargetException ex) {
                return;
            }
        }
    }*/
}
