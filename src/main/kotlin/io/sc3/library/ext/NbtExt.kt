package io.sc3.library.ext

import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import java.util.*

fun NbtCompound.byteToDouble(key: String): Double = getByte(key).toDouble()

fun NbtCompound.putOptInt(key: String, value: Int?) { value?.let { putInt(key, it) } }
fun NbtCompound.putOptString(key: String, value: String?) { value?.let { putString(key, it) } }
fun NbtCompound.putOptUuid(key: String, value: UUID?) { value?.let { putUuid(key, it) } }

fun NbtCompound.putNullableCompound(key: String, value: NbtCompound?) {
  if (value != null) put(key, value) else remove(key)
}

fun NbtCompound.putNullableUuid(key: String, value: UUID?) {
  if (value != null) putUuid(key, value) else remove(key)
}

fun NbtCompound.optBoolean(key: String): Boolean? =
  if (contains(key, NbtElement.BYTE_TYPE.toInt())) getByte(key) != 0.toByte() else null
fun NbtCompound.optInt(key: String): Int? =
  if (contains(key, NbtElement.INT_TYPE.toInt())) getInt(key) else null
fun NbtCompound.optString(key: String): String? =
  if (contains(key, NbtElement.STRING_TYPE.toInt())) getString(key) else null
fun NbtCompound.optCompound(key: String): NbtCompound? =
  if (contains(key, NbtElement.COMPOUND_TYPE.toInt())) getCompound(key) else null
fun NbtCompound.optUuid(key: String): UUID? =
  if (containsUuid(key)) getUuid(key) else null
