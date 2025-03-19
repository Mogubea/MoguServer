package me.mogubea.items;

import me.mogubea.attributes.Attribute;
import me.mogubea.items.reforges.ReforgeManager;
import me.mogubea.main.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MoguItemManager {

    private final Main plugin;
    private final Random random = new Random();
    private final ReforgeManager reforgeManager;
    private final Map<String, MoguItem> itemsByName = new LinkedHashMap<>();

    protected final NamespacedKey KEY_ID;

    // Anti dupe measures.
    public final NamespacedKey KEY_UUID;
    public final NamespacedKey KEY_CREATION_TIME;

    public MoguItemManager(Main plugin) {
        this.plugin = plugin;
        this.KEY_ID = new NamespacedKey(plugin, "ID");
        this.KEY_UUID = new NamespacedKey(plugin, "UUID");
        this.KEY_CREATION_TIME = new NamespacedKey(plugin, "CREATION_TIME");
        this.reforgeManager = new ReforgeManager(plugin);
    }

    public void registerItems() {
        if (MoguItems.TEST_FISH != null)
            plugin.getSLF4JLogger().info("Successfully registered " + itemsByName.size() + " custom items");
        reforgeManager.registerReforges();
    }
    
    protected MoguItem register(@NotNull MoguItem item) {
        if (itemsByName.containsKey(item.getIdentifier())) throw new UnsupportedOperationException("An item with the identifier \"" + item.getIdentifier() + "\" already exists!");
        itemsByName.put(item.getIdentifier(), item);
        item.postCreation();
        return item;
    }

    public @NotNull Collection<MoguItem> getItems() {
        return itemsByName.values();
    }

    /**
     * Attempt to grab a {@link MoguItem} instance from an {@link ItemStack}.
     * @param itemStack The ItemStack
     * @return Either the associated {@link MoguItem} or null.
     */
    public @Nullable MoguItem from(ItemStack itemStack) {
        if (!isItem(itemStack)) return null;

        PersistentDataContainer dataContainer = itemStack.getItemMeta().getPersistentDataContainer();
        return from(dataContainer.getOrDefault(KEY_ID, PersistentDataType.STRING, ""));
    }

    /**
     * Attempt to grab a {@link MoguItem} instance from a String.
     * @param identifier The identifier
     * @return Either the associated {@link MoguItem} or null.
     */
    public @Nullable MoguItem from(@NotNull String identifier) {
        return itemsByName.get(identifier.toUpperCase());
    }

    public @NotNull ItemStack formatItemStack(@NotNull ItemStack itemStack) {
        if (!isItem(itemStack)) return itemStack; // No point in formatting materials that can't be items anyway.
        MoguItem moguItem = from(itemStack);
        ItemMeta itemMeta = itemStack.getItemMeta();
        ItemRarity rarity = getItemRarity(itemStack);

        // Display Name
        Component displayName = itemMeta.displayName();

        if (displayName != null) {
            itemMeta.displayName(displayName.color(rarity.getColour()).decoration(TextDecoration.ITALIC, false));
        } else if (moguItem != null) {
            itemMeta.displayName(Component.text(moguItem.getDisplayName(), rarity.getColour()).decoration(TextDecoration.ITALIC, false));
        }

        // Custom Item lore, ignore vanilla items for now
        if (moguItem != null) {
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(rarity.getSmallName(), NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
            itemMeta.lore(lore);
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public @NotNull String getIdentifier(@NotNull ItemStack itemStack) {
        MoguItem moguItem = from(itemStack);
        return moguItem != null ? moguItem.getIdentifier() : itemStack.getType().name();
    }

    public @NotNull ItemRarity getItemRarity(@NotNull ItemStack itemStack) {
        MoguItem moguItem = from(itemStack);
        return moguItem == null ? ItemRarity.getVanillaRarity(itemStack) : moguItem.getRarity();
    }

    public void addTrackingTags(@NotNull ItemStack itemStack) {
        itemStack.editMeta(meta -> {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(KEY_UUID, PersistentDataType.STRING, UUID.randomUUID().toString());
            container.set(KEY_CREATION_TIME, PersistentDataType.LONG, System.currentTimeMillis());
        });
    }

    public double getBaseAttributeValue(@NotNull ItemStack itemStack, @NotNull Attribute attribute) {
        if (!isItem(itemStack)) return 0.0;
        return itemStack.getItemMeta().getPersistentDataContainer().getOrDefault(attribute.getNamespacedKey(), PersistentDataType.DOUBLE, 0.0);
    }

    public boolean isItem(@NotNull ItemStack itemStack) {
        return itemStack.getType().isItem() && !itemStack.getType().isEmpty();
    }

    public @NotNull Random getRandom() {
        return random;
    }

    protected @NotNull Main getPlugin() {
        return plugin;
    }

    public @NotNull ReforgeManager getReforgeManager() {
        return reforgeManager;
    }

    protected static @Nullable MoguItem fromStack(@NotNull ItemStack itemStack) {
        return Main.getInstance().getItemManager().from(itemStack);
    }

    protected static @NotNull String getItemIdentifier(@NotNull ItemStack itemStack) {
        return Main.getInstance().getItemManager().getIdentifier(itemStack);
    }
}
