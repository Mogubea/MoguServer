package me.mogubea.fishing;

import me.mogubea.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Random;

public class FishingSession {

    private final static Random random = new Random();
    private final static DecimalFormat floatFormat = new DecimalFormat("#.#");

    private final PlayerProfile profile;
    private final Player player;
    private final FishHook hook;

//    private float difficultyMultiplier = 1F;
    private float reelProgress;
    private int biteSessionReelCount;
    private boolean isBiting = false;

    private float fishStrength = 1F; // TODO: temporary variable

    protected FishingSession(@NotNull Player player, @NotNull FishHook hook) {
        this.player = player;
        this.hook = hook;

        hook.setMinWaitTime(15);
        hook.setMaxWaitTime((int) (40 + fishStrength));
        reelProgress = 15F + random.nextInt(16);
        biteSessionReelCount = 1;
        fishStrength = 1F + random.nextFloat(5);

        this.profile = PlayerProfile.from(player);
    }

    protected boolean onSuccessfulReel() {
        biteSessionReelCount += 1;
        reelProgress += (11F - fishStrength) * (2F / (biteSessionReelCount * biteSessionReelCount)); // Diminishing returns when spamming reel

        player.sendActionBar(Component.text("Reel progress: " + floatFormat.format(reelProgress) + "%", NamedTextColor.AQUA));

        hook.getWorld().spawnParticle(Particle.WATER_SPLASH, hook.getLocation(), 5);
        hook.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, hook.getLocation(), 4);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, Math.max(0.05F, 0.5F - 0.1F * biteSessionReelCount), 0.8F + (reelProgress/200F));

        if (reelProgress >= 100F)
            onSuccess();

        return reelProgress >= 100F;
    }

    protected float onFailedReel() {
        reelProgress -= 10F + fishStrength;
        player.sendActionBar(Component.text("Reel progress: " + floatFormat.format(reelProgress) + "%", NamedTextColor.AQUA));

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.6F, 0.8F);
        player.getWorld().spawnParticle(Particle.CRIT, hook.getLocation(), 4);

        if (reelProgress <= 0F)
            onFail();

        return reelProgress;
    }

    protected void onSessionTick() {
        if ((reelProgress -= 0.1F * fishStrength) <= 0F) {
            onFail();
        } else {
            player.sendActionBar(Component.text("Reel progress: " + floatFormat.format(reelProgress) + "%", NamedTextColor.AQUA));
        }
    }

    protected void onFail() {
        player.sendActionBar(Component.text("The catch got away...", NamedTextColor.RED));
        hook.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, hook.getLocation(), 3);

        synchronized (this) {
            hook.remove();
        }
    }

    protected void onSuccess() {

    }

    protected void setBiting(boolean isBiting) {
        this.isBiting = isBiting;
        this.biteSessionReelCount = 0;
    }

}
