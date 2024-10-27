package io.sc3.library.recipe

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.sc3.library.ScLibrary
import io.sc3.library.ext.EnchantmentExt
import net.fabricmc.fabric.api.item.v1.EnchantingContext
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ItemEnchantmentsComponent
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.EnchantedBookItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.util.Identifier
import kotlin.jvm.optionals.getOrNull

open class IngredientEnchanted(
  private val enchantmentKey: RegistryKey<Enchantment>,
  private val minLevel: Int,
) : CustomIngredient {
  private var wrapper: WrapperLookup? = null;

  constructor(enchantmentKey: RegistryKey<Enchantment>,
              minLevel: Int,
    wrapper: WrapperLookup) : this(enchantmentKey, minLevel) {
      this.wrapper = wrapper;
    }

  override fun getMatchingStacks(): List<ItemStack> {
    val stacks = mutableListOf<ItemStack>()

    // TODO: this function, in this state is prone to crashes
    // if the EnchantmentExt hasn't cached all enchantements yet,
    // this will BREAK and no longer run!!!
    // TODO: PLSFIX ASAP
    if(this.wrapper != null) {
      val enchantment = EnchantmentExt.getEnchantment(this.wrapper!!, enchantmentKey)
      for (item in Registries.ITEM) {
        if (item.defaultStack.canBeEnchantedWith(enchantment, EnchantingContext.PRIMARY) || item is EnchantedBookItem) {
          for (level in minLevel..enchantment.value().maxLevel) {
            val stack = ItemStack(item)
            val map = ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
            map.set(enchantment, level);
            EnchantmentHelper.set(stack, map.build());
            stacks.add(stack)
          }
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
      if(i.key.getOrNull() == this.enchantmentKey) {
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
      return PacketCodec.tuple(
        EnchantmentExt.enchantmentKeysPacketCodec, { z -> z.enchantmentKey },
        PacketCodecs.INTEGER, { z -> z.minLevel },
        ::IngredientEnchanted
      )
    }
  }
}
