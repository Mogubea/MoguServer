package me.mogubea.items;

import me.mogubea.entities.MoguEntityCrop;
import me.mogubea.events.MoguCropInteractEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MoguItemWitheredBonemeal extends MoguItem implements ICustomCropInteract, IEntityInteract {
    private final UUID jetsUUID = UUID.fromString("57ce0fda-4766-4718-8619-fe01b82ac0ab");

    protected MoguItemWitheredBonemeal(@NotNull MoguItemManager manager, @NotNull String identifier, @NotNull String displayName, @NotNull Material material, @NotNull ItemRarity rarity) {
        super(manager, identifier, displayName, material, rarity);
        setCustomModelData(1);
    }

    @Override
    public void onInteract(@NotNull PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!(block.getBlockData() instanceof Ageable crop)) return;

        switch (block.getType()) {
            case WHEAT, POTATOES, CARROTS, BEETROOTS, SWEET_BERRY_BUSH, MELON_STEM, PUMPKIN_STEM -> {
                if (!createDeadBush(crop.getAge(), block.getLocation(), true)) {
                    crop.setAge(Math.max(0, crop.getAge() - getRandom().nextInt(1, 3)));
                    block.setBlockData(crop);
                }

                playUseEffect(block.getLocation(), true);
            }
        }

        event.setUseItemInHand(Event.Result.DENY); // Disallow any other interactions
    }

    @Override
    public void onCustomCropInteract(@NotNull MoguCropInteractEvent event) {
        MoguEntityCrop crop = event.getCrop();

        if (createDeadBush(crop.getAge(), crop.getTrueLocation(), false)) {
            crop.discard();
        } else {
            crop.setAge(crop.getAge() - 1);
        }

        playUseEffect(crop.getTrueLocation(), false);
        event.setInteractionResult(true);
        event.setConsumeItem();
    }

    @Override
    public void onEntityInteract(@NotNull PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player target)) return;
        if (!target.getUniqueId().equals(jetsUUID)) return;

        target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0));
    }

    private boolean createDeadBush(int age, @NotNull Location location, boolean center) {
        if (age >= 1) return false;
        location.getBlock().setType(Material.DEAD_BUSH, false);
        if (location.subtract(0, 1, 0).getBlock().getType() == Material.FARMLAND)
            location.getBlock().setType(Material.DIRT);
        location.getWorld().spawnParticle(Particle.BLOCK_DUST, location.add(center ? 0.5 : 0, 1.4, center ? 0.5 : 0), 10, 0.32, 0.05, 0.32, 0, location.getBlock().getBlockData());
        return true;
    }

    private void playUseEffect(@NotNull Location location, boolean center) {
        location.getWorld().playSound(location.add(center ? 0.5 : 0, 0.3, center ? 0.5 : 0), Sound.ITEM_BONE_MEAL_USE, 0.65F, 0.6F);
        location.getWorld().spawnParticle(Particle.SOUL, location, 5, 0.24, 0.15, 0.24, 0);
    }

}
