package io.sc3.library.recipe

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.sc3.library.ScLibrary
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.potion.Potion
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class IngredientBrew(
  private val effect: StatusEffect,
  private val potion: Potion
) : CustomIngredient {
  override fun getMatchingStacks() = listOf(
    setPotion(ItemStack(Items.POTION), potion),
    setPotion(ItemStack(Items.SPLASH_POTION), potion),
    setPotion(ItemStack(Items.LINGERING_POTION), potion)
  )

  private fun setPotion(itemStack: ItemStack, potion: Potion): ItemStack {
    val components = itemStack.components;
    val potionContents = components.get(DataComponentTypes.POTION_CONTENTS)!!
    potionContents.customEffects.addAll(potion.effects)
    return itemStack
  }

  override fun requiresTesting(): Boolean = true

  override fun test(target: ItemStack): Boolean {
    if (target.isEmpty) return false
    val components = target.components.get(DataComponentTypes.POTION_CONTENTS) ?: return false;

    return components.effects
      .any { it.effectType === effect }
  }

  override fun getSerializer(): CustomIngredientSerializer<*> = Serializer

  object Serializer : CustomIngredientSerializer<IngredientBrew> {
    private val ID = ScLibrary.ModId("brew")

    override fun getIdentifier(): Identifier = ID
    override fun getCodec(allowEmpty: Boolean): MapCodec<IngredientBrew>? {
      return RecordCodecBuilder.mapCodec {
        instance -> instance.group(
          Registries.STATUS_EFFECT.codec.fieldOf("effect").forGetter({
              r -> r.effect
          }),
          Registries.POTION.codec.fieldOf("potion").forGetter({
              r -> r.potion
          })
        ).apply(instance, ::IngredientBrew)
      }
    }

    override fun getPacketCodec(): PacketCodec<RegistryByteBuf, IngredientBrew> {
      return PacketCodec.of(Serializer::write, Serializer::read)
    }

    fun read(buf: PacketByteBuf): IngredientBrew {
      val effect = Registries.STATUS_EFFECT.get(buf.readVarInt())!!
      val potion = Registries.POTION.get(buf.readVarInt())!!
      return IngredientBrew(effect, potion)
    }

    fun write(ingredient: IngredientBrew, buf: PacketByteBuf) {
      buf.writeVarInt(Registries.STATUS_EFFECT.getRawId(ingredient.effect))
      buf.writeVarInt(Registries.POTION.getRawId(ingredient.potion))
    }
  }
}
