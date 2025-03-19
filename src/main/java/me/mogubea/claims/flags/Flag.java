package me.mogubea.claims.flags;

import me.mogubea.claims.ClaimBase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class Flag<T> {
	
	private final String identifier;
	private final String displayName;
	private final boolean inheritsFromWorld;
	private final List<TextComponent> description = new ArrayList<>();

	private boolean needsPermission = false;
	protected final T defaultValue;
	protected T worldDefaultValue;
	protected T playerDefaultValue;

	private Consumer<ClaimBase> onUpdate;

	protected Flag(@NotNull String name, @NotNull String displayName, T defaultValue, boolean inheritFromWorld) {
		this.identifier = name;
		this.displayName = displayName;
		this.inheritsFromWorld = inheritFromWorld;
		this.defaultValue = defaultValue;
	}
	
	/**
	 * @return The flag's identifier. This should never be changed once used on the live server.
	 */
	public @NotNull String getIdentifier() {
		return identifier;
	}

	public @NotNull String getDisplayName() {
		return displayName;
	}

	/**
	 * @return The default value for this flag inside regular regions.
	 */
	public @NotNull T getDefault() {
		return defaultValue;
	}

	/**
	 * @return The default value for this flag inside world regions.
	 */
	public @NotNull T getWorldDefault() {
		return worldDefaultValue;
	}

	/**
	 * This value will be the same as {@link #getDefault()} by default unless stated otherwise.
	 * @return The default value for this flag inside player regions.
	 */
	public @NotNull T getPlayerDefault() {
		return playerDefaultValue;
	}

	@SuppressWarnings("unchecked")
	protected @NotNull <F extends Flag<?>> F setPlayerDefault(T def) {
		this.playerDefaultValue = def;
		return (F) this;
	}

	@SuppressWarnings("unchecked")
	protected @NotNull <F extends Flag<?>> F setWorldDefault(T def) {
		this.worldDefaultValue = def;
		return (F) this;
	}

	/**
	 * Determine if this flag for non-world regions will inherit the value from a world region, assuming they don't have an overriding default.
	 */
	public boolean inheritsFromWorld() {
		return inheritsFromWorld;
	}
	
	/**
	 * Sets the description for this flag, is only used in the {@link Flags} class when declaring a flag.
	 * <p>Descriptions should be divided into multiple lines of {@link Component}s to best fit item lore as
	 * this is where most players will be seeing information about a region flag.
	 * @param desc - The desired description of this flag.
	 * @throws UnsupportedOperationException if an attempt to change the description is made.
	 * @return The flag.
	 */
	@SuppressWarnings("unchecked")
	public @NotNull <F extends Flag<?>> F setDescription(TextComponent...desc) {
		if (!this.description.isEmpty()) throw new UnsupportedOperationException("A description was already given to Flag \""+ identifier +"\".");
		final int size = desc.length;
		for (int x = -1; ++x < size;)
			this.description.add(desc[x]);
		return (F) this;
	}

	/**
	 * Sets the description for this flag, is only used in the {@link Flags} class when declaring a flag.
	 * <p>Descriptions should be divided into multiple lines of {@link Component}s to best fit item lore as
	 * this is where most players will be seeing information about a region flag.
	 * @param desc - The desired description of this flag.
	 * @throws UnsupportedOperationException if an attempt to change the description is made.
	 * @return The flag.
	 */
	@SuppressWarnings("unchecked")
	public @NotNull <F extends Flag<?>> F setDescription(List<TextComponent> desc) {
		if (!this.description.isEmpty()) throw new UnsupportedOperationException("A description was already given to Flag \""+ identifier +"\".");
		this.description.addAll(desc);
		return (F) this;
	}

	/**
	 * @return The description given to the flag on server boot.
	 */
	public @NotNull List<TextComponent> getDescription() {
		return description;
	}

	@SuppressWarnings("unchecked")
	public @NotNull <F extends Flag<?>> F setConsumerOnUpdate(Consumer<ClaimBase> consumer) {
		if (this.onUpdate != null) throw new UnsupportedOperationException("An onUpdate consumer was already given to Flag \""+ identifier +"\".");
		this.onUpdate = consumer;
		return (F) this;
	}

	public void onUpdate(ClaimBase claim) {
		if (this.onUpdate != null)
			onUpdate.accept(claim);
	}

	/**
	 * Mark the flag as needing an explicit permission string to be changed by players.<br><br>
	 * This permission string is formatted as such: <b>bean.region.flag.{@link #getIdentifier()}</b>.
	 */
	@SuppressWarnings("unchecked")
	public @NotNull <F extends Flag<T>> F setNeedsPermission() {
		this.needsPermission = true;
		return (F) this;
	}
	
	/**
	 * @return "mogu.claim.flag." + {@link #getIdentifier()}.
	 */
	public @NotNull String getPermission() {
		return "mogu.claim.flag." + getIdentifier();
	}
	
	public boolean needsPermission() {
		return this.needsPermission;
	}

	/**
	 * Validates the value given. For example; if there's an invalid entry, change it.
	 */
	public abstract T validateValue(T o);
	
	public abstract T parseInput(String input);
	
	public abstract T unmarshal(String o);
	
	public abstract String marshal(T o);

}
