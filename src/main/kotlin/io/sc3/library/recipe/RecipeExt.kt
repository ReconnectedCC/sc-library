package io.sc3.library.recipe

import net.minecraft.data.server.recipe.ComplexRecipeJsonBuilder
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.item.DyeItem
import net.minecraft.item.ItemStack
import net.minecraft.recipe.CraftingRecipe
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.registry.Registries
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier

fun <T : CraftingRecipe> specialRecipe(
  exporter: RecipeExporter,
  recipe: SpecialCraftingRecipe
): Identifier {
  val recipeId = Registries.RECIPE_SERIALIZER.getId(recipe.serializer)
    ?: throw IllegalStateException("Recipe serializer $recipe is not registered")
  ComplexRecipeJsonBuilder.create({ _ -> recipe}).offerTo(exporter, recipeId.toString())
  return recipeId
}

fun itemDyeColor(item: ItemStack): DyeColor? =
  (item.item as? DyeItem)?.color
