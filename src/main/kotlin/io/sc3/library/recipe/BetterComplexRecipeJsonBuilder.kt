package io.sc3.library.recipe

import net.minecraft.advancement.*
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.item.ItemConvertible
import net.minecraft.recipe.CraftingRecipe
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.recipe.book.CraftingRecipeCategory.MISC
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.util.function.Consumer

class BetterComplexRecipeJsonBuilder<T : CraftingRecipe>(
  output: ItemConvertible,
  private val specialSerializer: SpecialRecipeSerializer<T>
) {
  private val outputItem = output.asItem()
  private val complexAdvancementBuilder: Advancement.Builder = Advancement.Builder.create()

  fun criterion(name: String, conditions: AdvancementCriterion<*>) = apply {
    complexAdvancementBuilder.criterion(name, conditions)
  }

  fun offerTo(exporter: Consumer<RecipeExporter>, recipeId: Identifier = itemId(outputItem)) {
    exporter.accept(object : RecipeExporter {
      override fun accept(recipeId: Identifier?, recipe: Recipe<*>?, advancement: AdvancementEntry?) {
      }

      override fun getAdvancementBuilder(): Advancement.Builder {
        return complexAdvancementBuilder
          /*TODO(please readd .parent(Identifier("recipe/root") w/o using Identifier)*/
          .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
          .rewards(AdvancementRewards.Builder.recipe(recipeId))
          .criteriaMerger(AdvancementRequirements.CriterionMerger.OR)
      }
    })
  }

  companion object {
    private fun itemId(item: ItemConvertible) = Registries.ITEM.getId(item.asItem())
  }
}
