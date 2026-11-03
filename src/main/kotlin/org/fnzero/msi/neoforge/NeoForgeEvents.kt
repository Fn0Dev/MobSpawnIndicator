//? if neoforge {
package org.fnzero.msi.neoforge

import net.minecraft.client.Minecraft
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.InputEvent
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.client.event.RenderLevelStageEvent
import org.fnzero.msi.MsiConfig
import org.fnzero.msi.MsiKeys
import org.fnzero.msi.MsiState
import org.fnzero.msi.SpawnRenderer
import org.fnzero.msi.client.gui.SpawnSettingsScreen

/** NeoForge event glue; all logic lives in the loader-agnostic classes. */
object NeoForgeEvents {

  @SubscribeEvent
  fun on_register_keys(event: RegisterKeyMappingsEvent) {
    MsiKeys.create()
    event.register(MsiKeys.toggle_indicator)
  }

  @SubscribeEvent
  fun on_key_in(event: InputEvent.Key) {
    while (MsiKeys.toggle_indicator.consumeClick()) MsiState.toggle()
  }

  @SubscribeEvent
  fun on_client_tick(event: ClientTickEvent.Pre) {
    SpawnRenderer.client_tick()
  }

  // NeoForge 21.6+ fires each render stage as its own event subclass; older
  // lines used a single event with a Stage enum. The camera is not carried by
  // the newer events, so it is always read from the Minecraft client.
  //? if >=1.21.6 {
  @SubscribeEvent
  fun on_render(event: RenderLevelStageEvent.AfterTranslucentBlocks) {
    render_overlay(event.poseStack)
  }
  //?} else {
  @SubscribeEvent
  fun on_render(event: RenderLevelStageEvent) {
    if (event.stage != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return
    render_overlay(event.poseStack)
  }
  //?}

  private fun render_overlay(pose_stack: com.mojang.blaze3d.vertex.PoseStack) {
    if (!MsiState.enabled || !MsiConfig.loaded) return

    val mc = Minecraft.getInstance()
    SpawnRenderer.render_overlay(
      pose_stack,
      SpawnRenderer.camera_pos(mc),
      mc.renderBuffers().bufferSource(),
    )
  }

  @SubscribeEvent
  fun on_register_commands(event: RegisterClientCommandsEvent) {
    event.dispatcher.register(
      Commands.literal("msi").then(
        Commands.literal("settings").executes {
          Minecraft.getInstance().setScreen(SpawnSettingsScreen())
          1
        }
      ).then(
        Commands.literal("toggle").executes { _ ->
          MsiState.toggle()
          1
        }
      ).then(
        Commands.literal("on").executes { _ ->
          MsiState.set(true)
          1
        }
      ).then(
        Commands.literal("off").executes { _ ->
          MsiState.set(false)
          1
        }
      ).then(
        Commands.literal("reset").executes { _ ->
          MsiConfig.reset_defaults()
          MsiState.send_status_reset()
          1
        }
      )
    )
  }
}
//?}
