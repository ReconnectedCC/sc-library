package io.sc3.library.recipe

import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.registry.RegistryWrapper

interface RecipeHandler {
  fun registerSerializers() {}
  fun generateRecipes(exporter: RecipeExporter, wrapper: RegistryWrapper.WrapperLookup) {}
}
