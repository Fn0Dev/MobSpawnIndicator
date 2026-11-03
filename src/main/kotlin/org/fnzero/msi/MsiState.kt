package org.fnzero.msi

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

/** Shared client-side mod state and status messaging, loader-agnostic. */
object MsiState {
  const val MOD_ID = "msi"

  @JvmStatic var enabled = false

  fun toggle() {
    set(!enabled)
  }

  fun set(value: Boolean) {
    enabled = value
    send_status()
  }

  private fun send_status() {
    val mc = Minecraft.getInstance()
    mc.player?.sendSystemMessage(
      Component.literal(
        if (enabled) "\u00a7a[Mob Spawn Indicator] Enabled"
        else "\u00a7c[Mob Spawn Indicator] Disabled"
      )
    )
  }

  fun send_status_reset() {
    val mc = Minecraft.getInstance()
    mc.player?.sendSystemMessage(
      Component.literal("\u00a7a[Mob Spawn Indicator] Config reset to defaults")
    )
  }
}
