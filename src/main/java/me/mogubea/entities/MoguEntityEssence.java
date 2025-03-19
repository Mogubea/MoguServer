package me.mogubea.entities;

import me.mogubea.guilds.Guild;
import me.mogubea.guilds.GuildData;
import me.mogubea.guilds.Guilds;
import me.mogubea.profile.PlayerProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;

public class MoguEntityEssence extends ArmorStand implements IMoguEntity {

    private short age;
    private int value;

    private Guild essenceType = Guilds.MINING;
    private Player followingPlayer;
    protected final org.bukkit.entity.ArmorStand bukkitEntity;

    protected MoguEntityEssence(Location location) {
        super(EntityType.ARMOR_STAND, ((CraftWorld)location.getWorld()).getHandle());
        setInvulnerable(true);
        setRot(0, 0);
        moveTo(location.getX() - 0.5 + random.nextDouble(), location.getY() - 0.3 + random.nextDouble(), location.getZ() - 0.5 + random.nextDouble());
        value = 1;
        persist = false;
//      setMarker(true);
        bukkitEntity = (org.bukkit.entity.ArmorStand) getBukkitEntity();
        bukkitEntity.setRightArmPose(new EulerAngle(0, 0, 0));
        bukkitEntity.setPersistent(false);
    }

    @Override
    public void tick() {
        super.tick();
        if (value < 1) {
            this.discard();
            return;
        }

        System.out.println("I am: " + getX() + ", " + getY() + ", " + getZ());

        Player prevTarget = this.followingPlayer;
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        if (this.isEyeInFluid(FluidTags.WATER)) {
            this.setUnderwaterMovement();
        } else if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.03, 0.0));
        }

        if (this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA)) {
            this.setDeltaMovement((this.random.nextFloat() - this.random.nextFloat()) * 0.2F, 0.20000000298023224, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        }

        if (!this.level().noCollision(this.getBoundingBox())) {
            this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
        }

        if (this.tickCount % 20 == 1) {
            this.scanForEntities();
        }

        if (this.tickCount % 8 == 1) {
            getLocation().getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, getLocation(), 1);
        }

        if (this.followingPlayer != null && (this.followingPlayer.isSpectator() || this.followingPlayer.isDeadOrDying())) {
            this.followingPlayer = null;
        }

        boolean cancelled = false;
        if (this.followingPlayer != prevTarget) {
            EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(this, this.followingPlayer, this.followingPlayer != null ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.FORGOT_TARGET);
            LivingEntity target = event.getTarget() == null ? null : ((CraftLivingEntity)event.getTarget()).getHandle();
            cancelled = event.isCancelled();
            if (cancelled) {
                this.followingPlayer = prevTarget;
            } else {
                this.followingPlayer = target instanceof Player ? (Player)target : null;
            }
        }

        if (this.followingPlayer != null && !cancelled) {
            Vec3 vec3d = new Vec3(this.followingPlayer.getX() - this.getX(), this.followingPlayer.getY() + (double)this.followingPlayer.getEyeHeight() / 2.0 - this.getY(), this.followingPlayer.getZ() - this.getZ());
            double d0 = vec3d.lengthSqr();
            if (d0 < 64.0) {
                double d1 = 1.0 - Math.sqrt(d0) / 8.0;
                this.setDeltaMovement(this.getDeltaMovement().add(vec3d.normalize().scale(d1 * d1 * 0.1)));
            }
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        float f = 0.98F;
        if (this.onGround) {
            f = this.level().getBlockState(BlockPos.containing(this.getX(), this.getY() - 1.0, this.getZ())).getBlock().getFriction() * 0.98F;
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(f, 0.98, f));
        if (this.onGround) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, -0.9, 1.0));
        }

        ++this.age;
        if (this.age >= 1000) {
            this.discard();
        }
    }

    @Override
    public void playerTouch(Player entityhuman) {
        if (!this.level().isClientSide && entityhuman.takeXpDelay == 0) {
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) entityhuman.getBukkitEntity();
            PlayerProfile profile = PlayerProfile.from(player);
            GuildData guildData = profile.getGuildData().getGuildData(getEssenceType());

//            if (!guildData.hasStone()) return;

            profile.getGuildData().getGuildData(getEssenceType()).addEssence(value);
            getLocation().getWorld().playSound(getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4F, 0.5F);
            entityhuman.takeXpDelay = 1;

            this.discard();
        }
    }

    @Override
    public InteractionResult interactAt(Player entityhuman, Vec3 vec3d, InteractionHand enumhand) {
        return InteractionResult.FAIL;
    }

    @Override
    public void postCreation() {
    }

    @Override
    public void transferData(Entity oldEntity) {

    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    public void setEssenceType(Guild guild) {
        this.essenceType = guild;

        // Render the custom model in the resource pack
        ItemStack paper = new ItemStack(Material.PAPER);
        paper.editMeta(meta -> meta.setCustomModelData(guild.getEssenceModelData()));
        bukkitEntity.setItem(EquipmentSlot.HAND, paper);
        detectEquipmentUpdatesPublic();
    }

    public void setValue(int value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    protected @NotNull Location getLocation() {
        return bukkitEntity.getLocation();
    }

    public Guild getEssenceType() {
        return essenceType;
    }

    private void setUnderwaterMovement() {
        Vec3 vec3d = this.getDeltaMovement();
        this.setDeltaMovement(vec3d.x * 0.9900000095367432, Math.min(vec3d.y + 5.000000237487257E-4, 0.05999999865889549), vec3d.z * 0.9900000095367432);
    }

    private void scanForEntities() {
        if (this.followingPlayer == null || this.followingPlayer.distanceToSqr(this) > 64.0) {
            this.followingPlayer = this.level().getNearestPlayer(this, 8.0);
        }
    }

}
