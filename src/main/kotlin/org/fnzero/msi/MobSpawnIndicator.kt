package org.fnzero.msi

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.options.OptionsScreen
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.client.event.InputEvent
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent
import net.neoforged.neoforge.client.event.ScreenEvent
import net.neoforged.neoforge.common.NeoForge
import org.fnzero.msi.client.gui.MsiButton
import org.fnzero.msi.client.gui.SpawnSettingsScreen

@Mod(MobSpawnIndicator.MOD_ID)
class MobSpawnIndicator(mod_event_bus: IEventBus, mod_container: ModContainer) {
  companion object {
    const val MOD_ID = "msi"
    @JvmStatic var enabled = false
  }

  init {
    mod_container.registerConfig(ModConfig.Type.COMMON, SpawnConfig.COMMON_SPEC)

    mod_event_bus.register(SpawnIndicatorKeys)
    NeoForge.EVENT_BUS.register(this)
    NeoForge.EVENT_BUS.register(SpawnRenderer)
  }

  @SubscribeEvent
  fun on_key_in(event: InputEvent.Key) {
    while (SpawnIndicatorKeys.toggle_indicator.consumeClick()) {
      enabled = !enabled
      val mc = Minecraft.getInstance()
      mc.player?.sendSystemMessage(
        Component.literal(
          if (enabled) "\u00a7a[Mob Spawn Indicator] Enabled"
          else "\u00a7c[Mob Spawn Indicator] Disabled"
        )
      )
    }
  }

  @SubscribeEvent
  fun on_screen_init(event: ScreenEvent.Init.Post) {
    val screen = event.screen
    if (screen !is OptionsScreen) return
    val sound_text = Component.translatable("options.sounds")
    for (child in screen.children()) {
      if (child is Button && child.message.string == sound_text.string) {
        val msi_btn =
          MsiButton(x = child.x + child.width + 2, y = child.y, width = 21, height = 20) {
            Minecraft.getInstance().setScreen(SpawnSettingsScreen(screen))
          }
        event.addListener(msi_btn)
        break
      }
    }
  }

  @SubscribeEvent
  fun on_register_commands(event: RegisterClientCommandsEvent) {
    val enabled_msg = Component.literal("\u00a7a[Mob Spawn Indicator] Enabled")
    val disabled_msg = Component.literal("\u00a7c[Mob Spawn Indicator] Disabled")

    event.dispatcher.register(
      Commands.literal("msi").then(
        Commands.literal("settings").executes {
          Minecraft.getInstance().setScreen(SpawnSettingsScreen())
          1
        }
      ).then(
        Commands.literal("toggle").executes { ctx ->
          enabled = !enabled
          ctx.source.sendSuccess({ if (enabled) enabled_msg else disabled_msg }, false)
          1
        }
      ).then(
        Commands.literal("on").executes { ctx ->
          enabled = true
          ctx.source.sendSuccess({ enabled_msg }, false)
          1
        }
      ).then(
        Commands.literal("off").executes { ctx ->
          enabled = false
          ctx.source.sendSuccess({ disabled_msg }, false)
          1
        }
      ).then(
        Commands.literal("reset").executes { ctx ->
          val cfg = SpawnConfig.COMMON
          cfg.render_rad.set(16)
          cfg.show_danger.set(true)
          cfg.show_warn.set(true)
          cfg.show_safe.set(false)
          cfg.overlay_opacity.set(0.5)
          SpawnConfig.COMMON_SPEC.save()
          ctx.source.sendSuccess({ Component.literal("\u00a7a[Mob Spawn Indicator] Config reset to defaults") }, false)
          1
        }
      )
    )
  }
}
