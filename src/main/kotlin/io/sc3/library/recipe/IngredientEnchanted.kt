package io.sc3.library.recipe

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.sc3.library.ScLibrary
import io.sc3.library.ext.EnchantmentExt
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ItemEnchantmentsComponent
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.EnchantedBookItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier

open class IngredientEnchanted(
  private val enchantmentKey: RegistryKey<Enchantment>,
  private val minLevel: Int,
) : CustomIngredient {
  private val enchantmentEntry = EnchantmentExt.getEnchantment(enchantmentKey);
  private val enchantment = enchantmentEntry.value()

  override fun getMatchingStacks(): List<ItemStack> {
    val stacks = mutableListOf<ItemStack>()

    // Find any item in the registry which matches this predicate
    for (item in Registries.ITEM) {
      if (enchantment.isAcceptableItem(item.defaultStack) || item is EnchantedBookItem) {
        for (level in minLevel..enchantment.maxLevel) {
          val stack = ItemStack(item)
          val map = ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
          map.set(enchantmentEntry, level);
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
      if(i.value() == this.enchantment) {
        return enchantmentsComponent.getLevel(i) >= minLevel
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
        EnchantmentExt.enchantmentKeysCodec.fieldOf("enchantment").forGetter {
          z -> z.enchantmentKey
        },
        Codec.INT.fieldOf("minLevel").forGetter { r ->
          r.minLevel
        }
      ).apply(instance, ::IngredientEnchanted)
      }
    }

    override fun getPacketCodec(): PacketCodec<RegistryByteBuf, IngredientEnchanted> {
      return PacketCodec.of(Serializer::write, Serializer::read)
    }

    fun read(buf: RegistryByteBuf): IngredientEnchanted {
      val enchantment = RegistryKey.of(RegistryKeys.ENCHANTMENT, buf.readIdentifier())

      val minLevel = buf.readVarInt()
      return IngredientEnchanted(enchantment, minLevel)
    }

    fun write(ingredient: IngredientEnchanted, buf: RegistryByteBuf) {
      buf.writeIdentifier(ingredient.enchantmentKey.value)
      buf.writeVarInt(ingredient.minLevel)
    }
  }
}
