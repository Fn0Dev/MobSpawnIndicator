package org.fnzero.msi

import net.minecraft.client.KeyMapping
import org.lwjgl.glfw.GLFW

/** Holds the toggle keybind; registration is done by the loader entry. */
object MsiKeys {
  lateinit var toggle_indicator: KeyMapping
    private set

  fun create() {
    toggle_indicator = KeyMapping("key.msi.toggle", GLFW.GLFW_KEY_J, "category.msi")
  }
}
