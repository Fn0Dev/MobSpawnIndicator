package org.fnzero.msi.client.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class MsiButton(x: Int, y: Int, width: Int, height: Int, private val on_click: () -> Unit) :
  AbstractWidget(x, y, width, height, Component.empty()) {

  override fun renderWidget(gui: GuiGraphics, mouse_x: Int, mouse_y: Int, partial_tick: Float) {
    gui.blit(TEXTURE, this.x, this.y, 0f, 0f, this.width, this.height, this.width, this.height)
    if (this.isHovered)
      gui.renderTooltip(Minecraft.getInstance().font, TOOLTIP, mouse_x, mouse_y)
  }

  override fun onClick(mouse_x: Double, mouse_y: Double) {
    on_click()
  }

  override fun updateWidgetNarration(output: NarrationElementOutput) {
    output.add(NarratedElementType.TITLE, TOOLTIP)
  }

  companion object {
    private val TEXTURE =
      ResourceLocation.fromNamespaceAndPath("msi", "textures/gui/btn_icon.png")
    private val TOOLTIP = Component.translatable("button.msi.settings.tooltip")
  }
}
