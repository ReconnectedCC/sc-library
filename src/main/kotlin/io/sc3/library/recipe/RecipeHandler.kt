package io.sc3.library.recipe

import net.minecraft.data.server.recipe.RecipeExporter
import java.util.function.Consumer

interface RecipeHandler {
  fun registerSerializers() {}
  fun generateRecipes(exporter: Consumer<RecipeExporter>) {}
}
