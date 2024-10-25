package io.sc3.library.ext

import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.registry.BuiltinRegistries
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.Identifier
import net.minecraft.world.World
import java.rmi.registry.Registry
import java.util.*


class EnchantmentExt {
  companion object {
    val enchantmentKeysCodec = RegistryKey.createCodec(RegistryKeys.ENCHANTMENT)

    fun getEnchantment(key: RegistryKey<Enchantment>): RegistryEntry<Enchantment> {
      return BuiltinRegistries.createWrapperLookup().createRegistryLookup().getOptionalEntry(
        RegistryKeys.ENCHANTMENT, key
      ).orElseThrow()
    }
    fun getEnchantment(world: World, key: RegistryKey<Enchantment>): RegistryEntry<Enchantment> {
      return world.registryManager.get(RegistryKeys.ENCHANTMENT).getEntry(key).orElseThrow()
    }
    fun getEnchantment(world: World, key: Enchantment): RegistryEntry<Enchantment> {
      return world.registryManager.get(RegistryKeys.ENCHANTMENT).getEntry(key);
    }
    fun getEnchantment(world: World, key: Identifier): RegistryEntry<Enchantment> {
      return world.registryManager.get(RegistryKeys.ENCHANTMENT).getEntry(key).orElseThrow();
    }
  }
}
