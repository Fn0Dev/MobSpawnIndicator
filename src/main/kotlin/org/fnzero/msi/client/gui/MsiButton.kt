package org.fnzero.msi.client.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.Component
//? if >=1.21.8 {
import net.minecraft.client.renderer.RenderPipelines
//?}
//? if <1.21.8 {
import net.minecraft.client.renderer.RenderType
//?}
//? if >=1.21.11 {
import net.minecraft.resources.Identifier
//?} else {
import net.minecraft.resources.ResourceLocation
//?}

/** Small icon button that opens the settings screen. */
class MsiButton(x: Int, y: Int, width: Int, height: Int, on_click: () -> Unit) :
  Button(x, y, width, height, Component.empty(), Button.OnPress { on_click() }, DEFAULT_NARRATION) {

  init {
    setTooltip(Tooltip.create(TOOLTIP))
  }

  // The widget draw hook was reworked in MC 1.21.11's mappings: AbstractButton
  // made renderWidget final and renderContents became the draw hook.
  //? if >=1.21.11 {
  override fun renderContents(gui: GuiGraphics, mouse_x: Int, mouse_y: Int, partial_tick: Float) {
    draw_icon(gui)
  }
  //?} else {
  override fun renderWidget(gui: GuiGraphics, mouse_x: Int, mouse_y: Int, partial_tick: Float) {
    draw_icon(gui)
  }
  //?}

  private fun draw_icon(gui: GuiGraphics) {
    //? if >=1.21.8 {
    gui.blit(
      RenderPipelines.GUI_TEXTURED,
      TEXTURE,
      this.x,
      this.y,
      0f,
      0f,
      this.width,
      this.height,
      this.width,
      this.height,
    )
    //?} else if >=1.21.2 {
    gui.blit(
      { RenderType.guiTexturedOverlay(TEXTURE) },
      TEXTURE,
      this.x,
      this.y,
      0f,
      0f,
      this.width,
      this.height,
      this.width,
      this.height,
    )
    //?} else {
    gui.blit(TEXTURE, this.x, this.y, 0f, 0f, this.width, this.height, this.width, this.height)
    //?}
  }

  companion object {
    //? if >=1.21.11 {
    private val TEXTURE = Identifier.fromNamespaceAndPath("msi", "textures/gui/btn_icon.png")
    //?} else {
    private val TEXTURE = ResourceLocation.fromNamespaceAndPath("msi", "textures/gui/btn_icon.png")
    //?}
    private val TOOLTIP = Component.translatable("button.msi.settings.tooltip")
  }
}
