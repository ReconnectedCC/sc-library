package io.sc3.library.recipe

import net.minecraft.item.ItemStack
import net.minecraft.recipe.RawShapedRecipe
import net.minecraft.recipe.ShapedRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory

open class ExtendedShapedRecipe(
  group: String,
  category: CraftingRecipeCategory,
  val rawShapedRecipe: RawShapedRecipe,
  output: ItemStack,
) : ShapedRecipe(group, category, rawShapedRecipe, output)
