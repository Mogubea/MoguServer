package me.mogubea.recipes;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

public class RecipesShaped {

	private final RecipeManager manager;
	
	public RecipesShaped(@NotNull RecipeManager manager) {
		this.manager = manager;
	}
	
	private @NotNull ShapedRecipe shapedRecipe(@NotNull String name, @NotNull ItemStack result, @NotNull String...shape) {
		ShapedRecipe sr = new ShapedRecipe(new NamespacedKey(manager.getPlugin(), "shaped_" + name), result);
		sr.shape(shape);
		manager.addRecipe(sr);
		return sr;
	}
	
}
