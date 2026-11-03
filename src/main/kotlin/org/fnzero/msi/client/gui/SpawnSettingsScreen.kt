package org.fnzero.msi.client.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractSliderButton
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.fnzero.msi.MsiConfig
import org.fnzero.msi.MsiState

class SpawnSettingsScreen(private val last_screen: Screen? = null) :
  Screen(Component.translatable("screen.msi.settings")) {
  private var radius_slider: RadiusSlider? = null
  private var opacity_slider: OpacitySlider? = null
  private var danger_btn: Button? = null
  private var warn_btn: Button? = null
  private var safe_btn: Button? = null

  private var show_danger = true
  private var show_warn = true
  private var show_safe = false

  override fun init() {
    if (!MsiConfig.loaded) return

    show_danger = MsiConfig.show_danger
    show_warn = MsiConfig.show_warn
    show_safe = MsiConfig.show_safe

    val left = width / 2 - 110
    var y = height / 2 - 70

    addRenderableWidget(
      Button.builder(toggle_msg()) { button ->
          MsiState.toggle()
          button.message = toggle_msg()
        }
        .bounds(left, y, 220, 20)
        .build()
    )

    y += 28
    danger_btn =
      Button.builder(danger_msg()) { button ->
          show_danger = !show_danger
          button.message = danger_msg()
        }
        .bounds(left, y, 220, 20)
        .build()
    addRenderableWidget(danger_btn)

    y += 24
    warn_btn =
      Button.builder(warn_msg()) { button ->
          show_warn = !show_warn
          button.message = warn_msg()
        }
        .bounds(left, y, 220, 20)
        .build()
    addRenderableWidget(warn_btn)

    y += 24
    safe_btn =
      Button.builder(safe_msg()) { button ->
          show_safe = !show_safe
          button.message = safe_msg()
        }
        .bounds(left, y, 220, 20)
        .build()
    addRenderableWidget(safe_btn)

    y += 28
    radius_slider = RadiusSlider(left, y, 220, 20, MsiConfig.render_rad)
    addRenderableWidget(radius_slider)

    y += 24
    opacity_slider = OpacitySlider(left, y, 220, 20, MsiConfig.overlay_opacity)
    addRenderableWidget(opacity_slider)

    y += 32
    addRenderableWidget(
      Button.builder(Component.translatable("gui.done")) { _ ->
          save()
          minecraft?.setScreen(last_screen)
        }
        .bounds(left, y, 220, 20)
        .build()
    )
  }

  override fun render(gui: GuiGraphics, mouse_x: Int, mouse_y: Int, partial_tick: Float) {
    super.render(gui, mouse_x, mouse_y, partial_tick)
    gui.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF)
  }

  private fun toggle_msg(): Component {
    return Component.translatable(
      if (MsiState.enabled) "screen.msi.on" else "screen.msi.off"
    )
  }

  private fun danger_msg(): Component {
    return Component.translatable(
      if (show_danger) "screen.msi.danger_on" else "screen.msi.danger_off"
    )
  }

  private fun warn_msg(): Component {
    return Component.translatable(
      if (show_warn) "screen.msi.warning_on" else "screen.msi.warning_off"
    )
  }

  private fun safe_msg(): Component {
    return Component.translatable(if (show_safe) "screen.msi.safe_on" else "screen.msi.safe_off")
  }

  private fun save() {
    if (!MsiConfig.loaded) return
    MsiConfig.show_danger = show_danger
    MsiConfig.show_warn = show_warn
    MsiConfig.show_safe = show_safe
    radius_slider?.let { MsiConfig.render_rad = it.slide_val }
    opacity_slider?.let { MsiConfig.overlay_opacity = it.slide_val }
    MsiConfig.save()
  }

  private class RadiusSlider(x: Int, y: Int, width: Int, height: Int, current: Int) :
    AbstractSliderButton(x, y, width, height, Component.empty(), (current - 4.0) / 60.0) {

    var slide_val: Int = current

    init {
      updateMessage()
    }

    override fun updateMessage() {
      setMessage(Component.translatable("screen.msi.radius", slide_val))
    }

    override fun applyValue() {
      slide_val = (4 + (value * 60.0)).toInt().coerceIn(4, 64)
      updateMessage()
    }
  }

  private class OpacitySlider(x: Int, y: Int, width: Int, height: Int, current: Double) :
    AbstractSliderButton(x, y, width, height, Component.empty(), (current - 0.05) / 0.95) {

    var slide_val: Double = current

    init {
      updateMessage()
    }

    override fun updateMessage() {
      setMessage(
        Component.translatable("screen.msi.opacity", String.format("%.0f", slide_val * 100))
      )
    }

    override fun applyValue() {
      slide_val = (0.05 + value * 0.95).coerceIn(0.05, 1.0)
      updateMessage()
    }
  }
}
