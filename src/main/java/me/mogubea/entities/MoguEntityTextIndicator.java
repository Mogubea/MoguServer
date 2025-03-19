package me.mogubea.entities;

import net.kyori.adventure.text.Component;
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

public class MoguEntityTextIndicator extends ArmorStand implements IMoguEntity {

    protected final org.bukkit.entity.ArmorStand bukkitEntity;
    private int timeToLive = 20;

    protected MoguEntityTextIndicator(Location location) {
        super(EntityType.ARMOR_STAND, ((CraftWorld)location.getWorld()).getHandle());
        setInvulnerable(true);
        setInvisible(true);
        setMarker(true);
        moveTo(location.getX() - 0.5 + random.nextDouble(), location.getY() - 0.3 + random.nextDouble(), location.getZ() - 0.5 + random.nextDouble());
        bukkitEntity = (org.bukkit.entity.ArmorStand) getBukkitEntity();
        bukkitEntity.setPersistent(false);
    }

    @Override
    public void tick() {
        moveTo(getX(), getY() + 0.09, getZ());
        if (--timeToLive < 0)
            discard();
    }

    public void setText(Component name) {
        bukkitEntity.customName(name);
        bukkitEntity.setCustomNameVisible(true);
    }

    @Override
    public InteractionResult interactAt(Player entityhuman, Vec3 vec3d, InteractionHand enumhand) {
        return InteractionResult.FAIL;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public void postCreation() {

    }

    @Override
    public void transferData(Entity oldEntity) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource damagesource, float f) {
        return false;
    }

}
