package io.sc3.library.recipe

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.ShapelessRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.util.collection.DefaultedList
import java.util.function.Function

/**
 * The underlying structure of a [ShapelessRecipe]-esque recipe, useful for writing serialisation and deserialization
 * code.
 */
class ShapelessRecipeSpec private constructor(
  val group: String,
  val category: CraftingRecipeCategory,
  val output: ItemStack,
  val input: DefaultedList<Ingredient>
) {
  companion object {
    fun <T : ShapelessRecipe> codec(
      constructor: (String, CraftingRecipeCategory, ItemStack, DefaultedList<Ingredient>) -> T
    ): Codec<T> {
      return RecordCodecBuilder.create { instance ->
        instance.group(
          Codec.STRING.fieldOf("group").forGetter { r -> r.group },
          CraftingRecipeCategory.CODEC.fieldOf("category").forGetter { r -> r.category },
          ItemStack.CODEC.fieldOf("output").forGetter { r -> r.getResult(null) },
          Ingredient.DISALLOW_EMPTY_CODEC.listOf()
            .xmap({ ingredients -> DefaultedList.copyOf(Ingredient.EMPTY, *ingredients.toTypedArray()) }, Function.identity())
            .fieldOf("ingredient").forGetter { r -> r.ingredients }
        ).apply(instance, constructor)
      }
    }
  }
}
