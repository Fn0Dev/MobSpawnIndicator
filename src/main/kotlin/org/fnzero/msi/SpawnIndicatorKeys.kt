package org.fnzero.msi

import net.minecraft.client.KeyMapping
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import org.lwjgl.glfw.GLFW

object SpawnIndicatorKeys {
  lateinit var toggle_indicator: KeyMapping
    private set

  @SubscribeEvent
  fun register(event: RegisterKeyMappingsEvent) {
    toggle_indicator = KeyMapping("key.msi.toggle", GLFW.GLFW_KEY_J, "category.msi")
    event.register(toggle_indicator)
  }
}
