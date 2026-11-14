package org.fnzero.msi

import net.minecraft.client.KeyMapping
import org.lwjgl.glfw.GLFW
//? if >=1.21.11 {
import net.minecraft.resources.Identifier
//?} else if >=1.21.9 {
import net.minecraft.resources.ResourceLocation
//?}

object MsiKeys {
  lateinit var toggle_indicator: KeyMapping
    private set

  //? if >=1.21.9 {
  private val CATEGORY: KeyMapping.Category =
    //? if >=1.21.11 {
    KeyMapping.Category.register(Identifier.fromNamespaceAndPath("msi", "main"))
    //?} else {
    KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath("msi", "main"))
    //?}
  //?}

  fun create() {
    //? if >=1.21.9 {
    toggle_indicator = KeyMapping("key.msi.toggle", GLFW.GLFW_KEY_J, CATEGORY)
    //?} else {
    toggle_indicator = KeyMapping("key.msi.toggle", GLFW.GLFW_KEY_J, "category.msi")
    //?}
  }
}
