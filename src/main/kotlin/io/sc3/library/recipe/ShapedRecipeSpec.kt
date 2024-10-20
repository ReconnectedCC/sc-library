package io.sc3.library.recipe

import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.RawShapedRecipe
import net.minecraft.recipe.ShapedRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory

/**
 * The underlying structure of a [ShapedRecipe]-esque recipe, useful for writing serialisation and deserialization
 * code.
 */
class ShapedRecipeSpec private constructor(
  val group: String,
  val category: CraftingRecipeCategory,
  val rawShapedRecipe: RawShapedRecipe,
  val output: ItemStack,
) {
  fun write(buf: PacketByteBuf) {
    buf.writeString(group)
    buf.writeEnumConstant(category)

    rawShapedRecipe.writeToBuf(buf)

    buf.writeItemStack(output)
  }

  companion object {
    fun ofRecipe(recipe: ExtendedShapedRecipe) = ShapedRecipeSpec(
      recipe.group, recipe.category, recipe.rawShapedRecipe, recipe.getResult(null)
    )

    fun ofPacket(buf: PacketByteBuf): ShapedRecipeSpec {
      val group = buf.readString()
      val category = buf.readEnumConstant(CraftingRecipeCategory::class.java)

      val raw = RawShapedRecipe.readFromBuf(buf)
      val output = buf.readItemStack()

      return ShapedRecipeSpec(group, category, raw, output)
    }
  }
}
