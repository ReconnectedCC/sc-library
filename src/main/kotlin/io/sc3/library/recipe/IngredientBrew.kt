package io.sc3.library.recipe

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.sc3.library.ScLibrary
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.PacketByteBuf
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionUtil
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class IngredientBrew(
  private val effect: StatusEffect,
  private val potion: Potion
) : CustomIngredient {
  override fun getMatchingStacks() = listOf(
    PotionUtil.setPotion(ItemStack(Items.POTION), potion),
    PotionUtil.setPotion(ItemStack(Items.SPLASH_POTION), potion),
    PotionUtil.setPotion(ItemStack(Items.LINGERING_POTION), potion)
  )

  override fun requiresTesting(): Boolean = true

  override fun test(target: ItemStack): Boolean {
    if (target.isEmpty) return false
    return PotionUtil.getPotionEffects(target)
      .any { it.effectType === effect }
  }

  override fun getSerializer(): CustomIngredientSerializer<*> = Serializer

  object Serializer : CustomIngredientSerializer<IngredientBrew> {
    private val ID = ScLibrary.ModId("brew")

    override fun getIdentifier(): Identifier = ID
    override fun getCodec(allowEmpty: Boolean): Codec<IngredientBrew> {
      return RecordCodecBuilder.create {
        instance -> instance.group(
          Registries.STATUS_EFFECT.codec.fieldOf("effect").forGetter({
              r -> r.effect
          }),
          Registries.POTION.codec.fieldOf("effect").forGetter({
              r -> r.potion
          })
        ).apply(instance, ::IngredientBrew)
      }
    }

    override fun read(buf: PacketByteBuf): IngredientBrew {
      val effect = buf.readRegistryValue(Registries.STATUS_EFFECT)!!
      val potion = buf.readRegistryValue(Registries.POTION)!!
      return IngredientBrew(effect, potion)
    }

    override fun write(buf: PacketByteBuf, ingredient: IngredientBrew) {
      buf.writeRegistryValue(Registries.STATUS_EFFECT, ingredient.effect)
      buf.writeRegistryValue(Registries.POTION, ingredient.potion)
    }
  }
}
