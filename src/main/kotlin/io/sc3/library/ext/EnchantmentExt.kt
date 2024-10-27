package io.sc3.library.ext

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.enchantment.Enchantment
import net.minecraft.network.codec.PacketCodec
import net.minecraft.registry.*
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.Identifier
import java.util.HashMap


class EnchantmentExt {
  companion object {
    val enchantmentKeysCodec: Codec<RegistryKey<Enchantment>> = RegistryKey.createCodec(RegistryKeys.ENCHANTMENT)
    val enchantmentKeysPacketCodec: PacketCodec<ByteBuf, RegistryKey<Enchantment>> = RegistryKey.createPacketCodec(RegistryKeys.ENCHANTMENT)
    val enchantmentCache: HashMap<Identifier, RegistryEntry<Enchantment>> = HashMap();

    fun getEnchantment(key: RegistryKey<Enchantment>): RegistryEntry<Enchantment> {
      if(enchantmentCache.containsKey(key.value)) {
        return enchantmentCache[key.value]!!
      } else {
        val entry = this.getEnchantment(BuiltinRegistries.createWrapperLookup(), key)
        enchantmentCache[key.value] = entry
        return entry
      }
    }

    fun getEnchantment(rwwpl: RegistryWrapper.WrapperLookup, key: RegistryKey<Enchantment>): RegistryEntry<Enchantment> {
      return rwwpl.createRegistryLookup().getOptionalEntry(
        RegistryKeys.ENCHANTMENT, key
      ).orElseThrow()
    }

    fun getEnchantment(drm: DynamicRegistryManager, key: RegistryKey<Enchantment>): RegistryEntry<Enchantment> {
      return drm.get(RegistryKeys.ENCHANTMENT).getEntry(key).orElseThrow()
    }
    fun getEnchantment(drm: DynamicRegistryManager, key: Enchantment): RegistryEntry<Enchantment> {
      return drm.get(RegistryKeys.ENCHANTMENT).getEntry(key);
    }
    fun getEnchantment(drm: DynamicRegistryManager, key: Identifier): RegistryEntry<Enchantment> {
      return drm.get(RegistryKeys.ENCHANTMENT).getEntry(key).orElseThrow();
    }
  }
}
