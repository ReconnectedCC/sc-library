package io.sc3.library

import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

object ScLibrary : ModInitializer {
  val log = LoggerFactory.getLogger("ScLibrary")!!

  val modId = "sc-library"
  fun ModId(value: String) = Identifier.of(modId, value)

  override fun onInitialize() {
  }
}
