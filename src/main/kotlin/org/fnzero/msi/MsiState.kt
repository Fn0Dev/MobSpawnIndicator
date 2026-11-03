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
    val message =
      Component.literal(
        if (enabled) "\u00a7a[Mob Spawn Indicator] Enabled"
        else "\u00a7c[Mob Spawn Indicator] Disabled"
      )
    // The player message API: sendSystemMessage pre-1.21.2, displayClientMessage
    // from 1.21.2 through 1.21.11, sendSystemMessage again in 26.x.
    //? if >=26.1 {
    mc.player?.sendSystemMessage(message)
    //?} else if >=1.21.2 {
    mc.player?.displayClientMessage(message, false)
    //?} else {
    mc.player?.sendSystemMessage(message)
    //?}
  }

  fun send_status_reset() {
    val mc = Minecraft.getInstance()
    val message = Component.literal("\u00a7a[Mob Spawn Indicator] Config reset to defaults")
    //? if >=26.1 {
    mc.player?.sendSystemMessage(message)
    //?} else if >=1.21.2 {
    mc.player?.displayClientMessage(message, false)
    //?} else {
    mc.player?.sendSystemMessage(message)
    //?}
  }
}
