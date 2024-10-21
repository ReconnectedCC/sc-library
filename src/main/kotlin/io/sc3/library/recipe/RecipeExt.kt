package io.sc3.library.recipe

import net.minecraft.advancement.Advancement
import net.minecraft.advancement.AdvancementEntry
import net.minecraft.data.server.recipe.ComplexRecipeJsonBuilder
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.inventory.Inventory
import net.minecraft.item.DyeItem
import net.minecraft.item.ItemStack
import net.minecraft.recipe.*
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.world.World

fun <T : CraftingRecipe> specialRecipe(
  exporter: RecipeExporter,
  recipe: SpecialCraftingRecipe
): Identifier {
  val recipeId = Registries.RECIPE_SERIALIZER.getId(recipe.serializer)
    ?: throw IllegalStateException("Recipe serializer $recipe is not registered")
  ComplexRecipeJsonBuilder.create({ _ -> recipe}).offerTo(exporter, recipeId.toString())
  return recipeId
}

private class CustomRecipeWrapper<T : Inventory>(val originalRecipe: Recipe<T>, val customSerializer: RecipeSerializer<*>) :
  Recipe<T> {
  override fun matches(inventory: T, world: World?): Boolean {
    return originalRecipe.matches(inventory, world)
  }

  override fun craft(inventory: T, lookup: RegistryWrapper.WrapperLookup?): ItemStack? {
    return originalRecipe.craft(inventory, lookup)
  }

  override fun fits(width: Int, height: Int): Boolean {
    return originalRecipe.fits(width, height)
  }

  override fun getResult(registriesLookup: RegistryWrapper.WrapperLookup?): ItemStack? {
    return originalRecipe.getResult(registriesLookup)
  }

  override fun getSerializer(): RecipeSerializer<*> {
    return customSerializer
  }

  override fun getType(): RecipeType<*> {
    return originalRecipe.getType()
  }
}

fun CraftingRecipeJsonBuilder.offerTo(
  export: RecipeExporter,
  serializer: RecipeSerializer<*>,
  id: Identifier? = null
) {
  offerTo(object : RecipeExporter {
    override fun accept(recipeId: Identifier?, recipe: Recipe<*>, advancement: AdvancementEntry?) {
      export.accept(recipeId, CustomRecipeWrapper(recipe, serializer), advancement);
    }

    override fun getAdvancementBuilder(): Advancement.Builder {
      return export.advancementBuilder
    }
  },id ?: CraftingRecipeJsonBuilder.getItemId(outputItem))
}

fun itemDyeColor(item: ItemStack): DyeColor? =
  (item.item as? DyeItem)?.color
