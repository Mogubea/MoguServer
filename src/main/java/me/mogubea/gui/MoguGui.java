package me.mogubea.gui;

import me.mogubea.main.Main;
import me.mogubea.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A custom interface that can be instantiated by players.
 */
public abstract class MoguGui {

	protected final Player player;
	protected final Main plugin;

	protected int interactCooldown = 300;
	protected int presetSize = 27;
	protected InventoryType presetType;
	protected Component name = Component.text("Inventory");
	protected Component baseName;
	protected ItemStack[] presetInv;
	protected PlayerProfile profile; // Real
	protected PlayerProfile targetProfile; // Target
	protected int page;
	protected int data;
	protected Inventory inventory;

	protected MoguGui(@NotNull Player player) {
		this(player, 0, 0);
	}

	protected MoguGui(@NotNull Player player, int page, int data) {
		this.player = player;
		this.plugin = Main.getInstance();
		PlayerProfile ack = PlayerProfile.from(player);
//		if (!ack.profileOverride.equals(ack.getUniqueId()))
//			tpp = PlayerProfile.from(ack.profileOverride);
//		else
		targetProfile = ack;
		profile = ack;
		this.page = page;
		this.data = data;
	}
	
	public void onInventoryClosed(@NotNull InventoryCloseEvent e) {}

	public void onInventoryOpened() {}

	public void onInventoryClicked(@NotNull InventoryClickEvent e) {}

	public void onInventoryDrag(@NotNull InventoryDragEvent e) {
		for (int slot : e.getRawSlots()) {
			if (slot < inventory.getSize()) {
				e.setCancelled(true);
				return;
			}
		}
	}

	/**
	 * Fired by {@link me.mogubea.listeners.InventoryListener}.
	 * @param e The {@link InventoryClickEvent}.
	 * @return Whether {@link #onInventoryClicked(InventoryClickEvent)} will be fired or not.
	 */
	public boolean preInventoryClick(InventoryClickEvent e) {
		if (e.getAction() == InventoryAction.NOTHING) return false;

		e.setCancelled(true);
		final ItemStack i = e.getCurrentItem();
		if (i == null) return false;

		return interactCooldown <= 0 || !profile.onCdElseAdd("guiClick", interactCooldown);
	}

	public void openInventory() {
		playOpenSound();
		if (this.presetType != null && presetType.isCreatable())
			this.inventory = Bukkit.createInventory(player, presetType, name);
		else
			this.inventory = Bukkit.createInventory(player, presetSize, name);
		if (presetInv != null)
			inventory.setContents(presetInv);
		player.openInventory(inventory);
		profile.setMoguGui(this);
		
		onInventoryOpened();
	}
	
	public void pageUp() {
		this.page++;
		if (page > 50)
			page = 50;
		onInventoryOpened();
	}
	
	public void pageDown() {
		this.page--;
		if (page < 0)
			page = 0;
		onInventoryOpened();
	}
	
	public void setPage(int page) {
		this.page = page;
		if (page < 0 || page > 50)
			this.page = 0;
		onInventoryOpened();
	}
	
	public void setProfile(PlayerProfile pp) {
		this.targetProfile = pp;
		onInventoryOpened();
	}

	public @NotNull PlayerProfile getViewerProfile() {
		return profile;
	}

	public @NotNull PlayerProfile getViewedProfile() {
		return targetProfile;
	}

	public @NotNull Player getViewer() {
		return player;
	}

	public @NotNull Inventory getInventory() {
		return inventory;
	}
	
	public int getPage() {
		return page;
	}
	
	public int getData() {
		return data;
	}
	
	public int setData(int data) {
		return this.data = data;
	}
	
	public int upData() {
		return this.data++;
	}
	
	public int downData() {
		return this.data--;
	}
	
	protected void setName(@NotNull Component name) {
		this.name = name;
	}
	
	protected void setName(@NotNull String name) {
		this.name = Component.text(name);
	}

	public @NotNull Component getName() {
		return name;
	}
	
	protected boolean isOverrideView() {
		return targetProfile.getId() != profile.getId();
	}
	
	protected void playOpenSound() {
		player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.25F, 1.0F);
	}
	
	public void close() {
		player.closeInventory();
	}
	
	public void refresh() {
		onInventoryOpened();
	}

	/**
	 * Fires every 20 in-game ticks.
	 */
	public void onTick() {

	}

	/**
	 * Get all the currently viewed instances of the specified {@link MoguGui},
	 * this method can be very useful in a forEach loop to {@link MoguGui#refresh()} with a criteria.
	 */
	public static @NotNull <T extends MoguGui> Collection<T> getAllViewers(@Nonnull final Class<T> clazz) {
		ArrayList<T> instances = new ArrayList<>();
		Bukkit.getOnlinePlayers().forEach((p) -> {
			PlayerProfile pp = PlayerProfile.from(p);
			if (pp.getMoguGui() != null && clazz.isInstance(pp.getMoguGui()))
				instances.add(clazz.cast(pp.getMoguGui()));
		});
		return instances;
	}

	public @NotNull Main getPlugin() {
		return plugin;
	}

	/**
	 * Creates negative space. This depends on the resource pack. Maximum 511.
	 * @param pixels The amount of pixels to go back by
	 * @return Negative space.
	 */
	protected static @NotNull String negativeSpace(int pixels) {
		StringBuilder builder = new StringBuilder();

		for (int x = 8; --x > -1;) {
			int val = (int) Math.pow(2, x);
			if (pixels / val < 1) continue;
			pixels -= val;
			builder.append(StringEscapeUtils.unescapeJava("\\u4E0" + x));
		}

		return builder.toString();
	}

	@NotNull
	protected static ItemStack newItem(@NotNull ItemStack it, Component name, List<TextComponent> lore) {
		ItemStack i = it.clone();
		ItemMeta meta = i.getItemMeta();
		meta.displayName(name.decoration(TextDecoration.ITALIC, false));
		meta.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ITEM_SPECIFICS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
		meta.lore(lore);
		i.setItemMeta(meta);
		return i;
	}
	
	protected final static DecimalFormat df = new DecimalFormat("#,###");
	protected final static DecimalFormat dec = new DecimalFormat("#,###.##");
	
}
