//? if fabric {
package org.fnzero.msi.fabric

import net.fabricmc.api.ClientModInitializer
//? if >=26.1 {
import net.fabricmc.fabric.api.client.command.v2.ClientCommands as ClientCommandManager
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper as KeyBindingHelper
//?} else {
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
//?}
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
//? if >=1.21.11 {
//? if <26.1 {
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
//?}
//?}
//? if <=1.21.8 {
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
//?}
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import org.fnzero.msi.MsiConfig
import org.fnzero.msi.MsiKeys
import org.fnzero.msi.MsiState
import org.fnzero.msi.SpawnRenderer
import org.fnzero.msi.client.gui.SpawnSettingsScreen

class MsiFabricClient : ClientModInitializer {
  override fun onInitializeClient() {
    MsiConfig.init(FabricLoader.getInstance().getConfigDir())

    MsiKeys.create()
    //? if >=26.1 {
    KeyBindingHelper.registerKeyMapping(MsiKeys.toggle_indicator)
    //?} else {
    KeyBindingHelper.registerKeyBinding(MsiKeys.toggle_indicator)
    //?}

    ClientTickEvents.START_CLIENT_TICK.register { _ ->
      while (MsiKeys.toggle_indicator.consumeClick()) MsiState.toggle()
      SpawnRenderer.client_tick()
    }

    //? if >=1.21.11 {
    //? if <26.1 {
    WorldRenderEvents.BEFORE_TRANSLUCENT.register { ctx ->
      if (!MsiState.enabled || !MsiConfig.loaded) return@register
      val mc = Minecraft.getInstance()
      SpawnRenderer.render_overlay(
        ctx.matrices(),
        SpawnRenderer.camera_pos(mc),
        mc.renderBuffers().bufferSource(),
      )
    }
    //?}
    //?}
    //? if <=1.21.8 {
    WorldRenderEvents.AFTER_TRANSLUCENT.register { ctx ->
      if (!MsiState.enabled || !MsiConfig.loaded) return@register
      val pose_stack = ctx.matrixStack() ?: return@register
      val mc = Minecraft.getInstance()
      SpawnRenderer.render_overlay(
        pose_stack,
        SpawnRenderer.camera_pos(mc),
        mc.renderBuffers().bufferSource(),
      )
    }
    //?}

    ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
      dispatcher.register(
        ClientCommandManager.literal("msi").then(
          ClientCommandManager.literal("settings").executes { _ ->
            Minecraft.getInstance().execute {
              SpawnSettingsScreen.open(Minecraft.getInstance())
            }
            1
          }
        ).then(
          ClientCommandManager.literal("toggle").executes { _ ->
            MsiState.toggle()
            1
          }
        ).then(
          ClientCommandManager.literal("on").executes { _ ->
            MsiState.set(true)
            1
          }
        ).then(
          ClientCommandManager.literal("off").executes { _ ->
            MsiState.set(false)
            1
          }
        ).then(
          ClientCommandManager.literal("reset").executes { ctx ->
            MsiConfig.reset_defaults()
            ctx.source.sendFeedback(
              Component.literal("\u00a7a[Mob Spawn Indicator] Config reset to defaults")
            )
            1
          }
        )
      )
    }
  }
}
//?}
