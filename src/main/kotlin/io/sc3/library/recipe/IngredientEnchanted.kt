package io.sc3.library.recipe

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.sc3.library.ScLibrary
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ItemEnchantmentsComponent
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.EnchantedBookItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.util.*

class IngredientEnchanted(
  private val enchantment: Enchantment,
  private val minLevel: Int,
) : CustomIngredient {
  override fun getMatchingStacks(): List<ItemStack> {
    val stacks = mutableListOf<ItemStack>()

    // Find any item in the registry which matches this predicate
    for (item in Registries.ITEM) {
      if (enchantment.isAcceptableItem(item.defaultStack) || item is EnchantedBookItem) {
        for (level in minLevel..enchantment.maxLevel) {
          val stack = ItemStack(item)
          val map = ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
          map.set(enchantment, level);
          EnchantmentHelper.set(stack, map.build());
          stacks.add(stack)
        }
      }
    }

    return stacks
  }

  override fun requiresTesting(): Boolean = true

  override fun test(target: ItemStack?): Boolean {
    if (target == null || target.isEmpty) return false

    val enchantmentsComponent: ItemEnchantmentsComponent? = if (target.item === Items.ENCHANTED_BOOK) {
      target.components.get(DataComponentTypes.STORED_ENCHANTMENTS) // stored_enchantments is for books brah
    } else {
      target.enchantments
    }

    if(enchantmentsComponent == null) return false;

    for(i in  enchantmentsComponent.enchantments) {
      val itemEnchant = Registries.ENCHANTMENT.get(Identifier(i.idAsString));
      if(itemEnchant == this.enchantment) {
        return enchantmentsComponent.getLevel(itemEnchant) >= minLevel
      }
    }
    return false;
  }

  override fun getSerializer(): CustomIngredientSerializer<*> = Serializer

  object Serializer : CustomIngredientSerializer<IngredientEnchanted> {
    private val ID = ScLibrary.ModId("enchantment")
    override fun getIdentifier(): Identifier = ID
    override fun getCodec(allowEmpty: Boolean): MapCodec<IngredientEnchanted> {
      return RecordCodecBuilder.mapCodec {
          instance -> instance.group(
        Registries.ENCHANTMENT.codec.fieldOf("enchantment").forGetter({
            r -> r.enchantment
        }),
        Codec.INT.fieldOf("minLevel").forGetter({
          r -> r.minLevel
        })
      ).apply(instance, ::IngredientEnchanted)
      }
    }

    override fun getPacketCodec(): PacketCodec<RegistryByteBuf, IngredientEnchanted> {
      return PacketCodec.of(Serializer::write, Serializer::read)
    }

    fun read(buf: RegistryByteBuf): IngredientEnchanted {
      val enchantment = Registries.ENCHANTMENT.get(buf.readVarInt())!!
      val minLevel = buf.readVarInt()
      return IngredientEnchanted(enchantment, minLevel)
    }

    fun write(ingredient: IngredientEnchanted, buf: RegistryByteBuf) {
      buf.writeVarInt(Registries.ENCHANTMENT.getRawId(ingredient.enchantment))
      buf.writeVarInt(ingredient.minLevel)
    }
  }
}
