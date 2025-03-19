package me.mogubea.recipes;

import me.mogubea.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RecipeManager {

    private final Main plugin;
    private final List<Recipe> recipes = new ArrayList<>();

    public RecipeManager(Main plugin) {
        this.plugin = plugin;

        new RecipesShapeless(this);
        new RecipesShaped(this);
        new RecipesStoneCutter(this);

        this.recipes.forEach(Bukkit::addRecipe);
    }

    protected @NotNull Main getPlugin() {
        return plugin;
    }

    protected void addRecipe(@NotNull Recipe r) {
        recipes.add(r);
    }

    public void unregisterRecipes() {
        for (Recipe recipe : recipes)
            Bukkit.removeRecipe(((Keyed)recipe).getKey());
    }

}
