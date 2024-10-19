package io.sc3.library.recipe

import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.ShapelessRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.util.collection.DefaultedList

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
  fun write(buf: PacketByteBuf) {
    buf.writeString(group)
    buf.writeEnumConstant(category)
    buf.writeVarInt(input.size)
    for (ingredient in input) ingredient.write(buf)
    buf.writeItemStack(output)
  }

  companion object {

    fun ofRecipe(recipe: ShapelessRecipe) = ShapelessRecipeSpec(
      recipe.group, recipe.category, recipe.getResult(null) /* TODO(1.19.4) */, recipe.ingredients
    )

    fun ofPacket(buf: PacketByteBuf): ShapelessRecipeSpec {
      val group = buf.readString()
      val category = buf.readEnumConstant(CraftingRecipeCategory::class.java)

      val size = buf.readVarInt()
      val input = DefaultedList.ofSize(size, Ingredient.EMPTY)
      for (i in input.indices) input[i] = Ingredient.fromPacket(buf)

      val output = buf.readItemStack()
      return ShapelessRecipeSpec(group, category, output, input)
    }
  }
}
