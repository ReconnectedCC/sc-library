package io.sc3.library.recipe

import net.minecraft.advancement.*
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.recipe.*
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.recipe.book.CraftingRecipeCategory.MISC
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.world.World
import java.util.function.Function;


class BetterComplexRecipeJsonBuilder<T : CraftingRecipe>(
  output: ItemConvertible,
  private val recipe: Recipe<*>
) {
  private val outputItem = output.asItem()
  private val complexAdvancementBuilder: Advancement.Builder = Advancement.Builder.create()

  fun criterion(name: String, conditions: AdvancementCriterion<*>) = apply {
    complexAdvancementBuilder.criterion(name, conditions)
  }

  fun offerTo(exporter: RecipeExporter, recipeId: Identifier = itemId(outputItem)) {
    val advancementId = recipeId.withPrefixedPath("recipes/" + MISC.asString() + "/")
    val advancement = complexAdvancementBuilder
      .parent(Identifier("recipes/root")) /* TODO(.parent(Identifier) is marked for removal) */
      .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
      .rewards(AdvancementRewards.Builder.recipe(recipeId))
      .criteriaMerger(AdvancementRequirements.CriterionMerger.OR)
      .build(advancementId);

    exporter.accept(recipeId, recipe, advancement);
  }

  companion object {
    private fun itemId(item: ItemConvertible) = Registries.ITEM.getId(item.asItem())
  }
}
