package me.mogubea.recipes;

import me.mogubea.items.MoguItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.NotNull;

public class RecipesShapeless {

	private final RecipeManager manager;

	public RecipesShapeless(@NotNull RecipeManager manager) {
		this.manager = manager;
		ShapelessRecipe recipe;

		recipe = shapelessRecipe("withered_bone_meal", MoguItems.WITHERED_BONE_MEAL.getItemStack(3));
		recipe.addIngredient(MoguItems.WITHERED_BONE.getItemStack());
		recipe.setCategory(CraftingBookCategory.MISC);

		recipe = shapelessRecipe("withered_to_black_dye", new ItemStack(Material.BLACK_DYE));
		recipe.addIngredient(MoguItems.WITHERED_BONE_MEAL.getItemStack());
		recipe.setCategory(CraftingBookCategory.MISC);
	}
	
	private @NotNull ShapelessRecipe shapelessRecipe(@NotNull String name, @NotNull ItemStack result) {
		ShapelessRecipe sr = new ShapelessRecipe(new NamespacedKey(manager.getPlugin(), "shaped_" + name), result);
		manager.addRecipe(sr);
		return sr;
	}
	
}
