package io.sc3.library.recipe

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RawShapedRecipe
import net.minecraft.recipe.ShapedRecipe
import net.minecraft.recipe.ShapelessRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.util.collection.DefaultedList
import java.util.function.Function

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
  companion object {
    fun <T : ExtendedShapedRecipe> codec(
      constructor: (String, CraftingRecipeCategory, RawShapedRecipe, ItemStack) -> T
    ): MapCodec<T> {
      return RecordCodecBuilder.mapCodec { instance ->
        instance.group(
          Codec.STRING.fieldOf("group").forGetter { r -> r.group },
          CraftingRecipeCategory.CODEC.fieldOf("category").forGetter { r -> r.category },
          RawShapedRecipe.CODEC.fieldOf("recipe").forGetter { r -> r.rawShapedRecipe },
          ItemStack.CODEC.fieldOf("output").forGetter { z -> z.getResult(null) }
        ).apply(instance, constructor)
      }
    }

    fun <T : ExtendedShapedRecipe> packetCodec(
      constructor: (String, CraftingRecipeCategory, RawShapedRecipe, ItemStack) -> T
    ): PacketCodec<RegistryByteBuf, T> {
      return PacketCodec.tuple(
        PacketCodecs.STRING, ExtendedShapedRecipe::getGroup,
        CraftingRecipeCategory.PACKET_CODEC, ExtendedShapedRecipe::getCategory,
        RawShapedRecipe.PACKET_CODEC, ExtendedShapedRecipe::rawShapedRecipe,
        ItemStack.PACKET_CODEC, { a -> a.getResult(null) },
        constructor
      )
    }

    fun ofRecipe(recipe: ExtendedShapedRecipe) = ShapedRecipeSpec(
      recipe.group, recipe.category, recipe.rawShapedRecipe, recipe.getResult(null)
    )
  }
}
