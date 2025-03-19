package me.mogubea.entities;

import me.mogubea.claims.Claim;
import me.mogubea.claims.flags.Flags;
import me.mogubea.events.MoguCropInteractEvent;
import me.mogubea.items.MoguItem;
import me.mogubea.main.Main;
import me.mogubea.profile.PlayerProfile;
import me.mogubea.statistics.SimpleStatType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;

public abstract class MoguEntityCrop extends ArmorStand implements IMoguEntity {

    protected static final NamespacedKey KEY_AGE = new NamespacedKey(Main.getInstance(), "CROP_AGE");
    public static final String METADATA_TAG = "cropHolder";

    private byte age;
    private short timeUntilNextAge;
    protected final org.bukkit.entity.ArmorStand bukkitEntity;

    protected MoguEntityCrop(Location location) {
        super(EntityType.ARMOR_STAND, ((CraftWorld)location.getWorld()).getHandle());
        setInvulnerable(true);
        setInvisible(true);
        setRot(0, 0);
        moveTo(location.getBlockX() + 0.5, location.getY() + getYOffset(), location.getBlockZ() + 0.5);
        bukkitEntity = (org.bukkit.entity.ArmorStand) getBukkitEntity();
        bukkitEntity.setRightArmPose(new EulerAngle(0, 0, 0));
    }

    @Override
    public void tick() {
        if (age < getMaxAge() && --timeUntilNextAge <= 0)
            setAge(age + 1);
    }

    @Override
    public InteractionResult interactAt(Player entityhuman, Vec3 vec3d, InteractionHand enumhand) {
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) entityhuman.getBukkitEntity();

        MoguCropInteractEvent event = new MoguCropInteractEvent(player, this);

        // In order to respect the changes to the event when the event is called, these checks must be done in advance.
        boolean doBonemeal = !isReadyToHarvest() && event.getMoguItem() == null && event.getItemStack().getType() == Material.BONE_MEAL;

        if (isReadyToHarvest() || doBonemeal)
            event.setInteractionResult(true);
        if (doBonemeal)
            event.setConsumeItem();

        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return event.getInteractionResult();

        if (doBonemeal) {
            timeUntilNextAge -= getBonemealEffectiveness();
            bukkitEntity.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, getTrueLocation().add(0, 0.2, 0), 9, 0.22, 0.12, 0.22);
            bukkitEntity.getWorld().playSound(getTrueLocation().add(0.5, 0.5, 0.5), Sound.ITEM_BONE_MEAL_USE, 0.65F, 1F);
        }

        if (event.getItemConsumeCount() > 0 && player.getGameMode() != GameMode.CREATIVE)
            event.getItemStack().setAmount(event.getItemStack().getAmount() - event.getItemConsumeCount());

        if (event.isIgnoringHarvest() || !isReadyToHarvest())
            return event.getInteractionResult();

        // Past this line does not respect the event's set interaction result.

        Claim claim = Main.getInstance().getClaimManager().getClaim(getTrueLocation());
        if (claim != null && !claim.isTrustedFor(player, Flags.CROP_HARVESTING))
            return InteractionResult.FAIL;

        dropAndSounds();
        addStat(player);
        setAge(0);
        return InteractionResult.SUCCESS;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        remove(RemovalReason.KILLED);
        return PushReaction.IGNORE;
    }

    @Override
    public void postCreation() {
        setAge(age);
        getTrueLocation().subtract(0, 1, 0).getBlock().setMetadata(METADATA_TAG, new FixedMetadataValue(Main.getInstance(), null));
    }

    @Override
    public void transferData(Entity oldEntity) {
        PersistentDataContainer container = oldEntity.getPersistentDataContainer();
        age = container.getOrDefault(KEY_AGE, PersistentDataType.BYTE, (byte)0);
        setPos(getX(), getY() - getYOffset(), getZ());
    }

    @Override
    public boolean hurt(DamageSource damagesource, float f) {
        if (!(damagesource.getDirectEntity() instanceof Player playerHandle)) return false;
        org.bukkit.entity.Player player = (org.bukkit.entity.Player) playerHandle.getBukkitEntity();

        Claim claim = Main.getInstance().getClaimManager().getClaim(getTrueLocation());
        if (claim == null || claim.isTrustedFor(player, Flags.BUILD_ACCESS)) {
            addStat(player);
            remove(RemovalReason.KILLED);
        }
        return false;
    }

    @Override
    public void remove(RemovalReason entity_removalreason) {
        if (entity_removalreason == RemovalReason.KILLED)
            dropAndSounds();

        getTrueLocation().subtract(0, 1, 0).getBlock().removeMetadata(METADATA_TAG, Main.getInstance());
        super.remove(entity_removalreason);
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age > getMaxAge() ? getMaxAge() : (byte) age;
        this.timeUntilNextAge = generateTicksForNextAge();

        bukkitEntity.getPersistentDataContainer().set(KEY_AGE, PersistentDataType.BYTE, this.age);

        // Render the custom model in the resource pack
        ItemStack paper = new ItemStack(Material.PAPER);
        paper.editMeta(meta -> meta.setCustomModelData(getCustomModelData() + age));
        bukkitEntity.setItem(EquipmentSlot.HAND, paper);
        detectEquipmentUpdatesPublic();
    }

    public @NotNull Location getTrueLocation() {
        return getLocation().subtract(0, getYOffset(), 0);
    }

    protected @NotNull Location getLocation() {
        return bukkitEntity.getLocation();
    }

    protected void dropAndSounds() {
        bukkitEntity.getWorld().playSound(getTrueLocation().add(0.5, 0.5, 0.5), Material.WHEAT.createBlockData().getSoundGroup().getBreakSound(), 0.65F, 1F);
        bukkitEntity.getWorld().spawnParticle(Particle.ITEM_CRACK, getTrueLocation().add(0, 0.2, 0), 10, 0.32, 0.05, 0.32, 0, bukkitEntity.getItem(EquipmentSlot.HAND));
        bukkitEntity.getWorld().dropItem(getTrueLocation(), getSeeds().getItemStack());
        if (age >= getMaxAge())
            bukkitEntity.getWorld().dropItem(getTrueLocation(), getDrop());
    }

    private void addStat(org.bukkit.entity.Player player) {
        if (age >= getMaxAge())
            PlayerProfile.from(player).getStats().addToStat(SimpleStatType.CROP_HARVEST, getStatName(), 1);
    }

    /**
     * @return The maximum age for this crop.
     */
    public abstract byte getMaxAge();

    public boolean isReadyToHarvest() {
        return getAge() >= getMaxAge();
    }

    protected abstract short generateTicksForNextAge();

    protected abstract @NotNull MoguItem getSeeds();

    protected abstract @NotNull ItemStack getDrop();

    public abstract short getBonemealEffectiveness();

    public abstract int getEssenceValue();

    protected abstract @NotNull String getStatName();

    protected abstract int getCustomModelData();

    protected abstract double getYOffset();

}
