package org.fnzero.msi.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.fnzero.msi.client.gui.MsiButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin {

  @Inject(method = "repositionElements", at = @At("RETURN"))
  private void msi$repositionButton(CallbackInfo ci) {
    OptionsScreen screen = (OptionsScreen) (Object) this;
    Component sound_text = Component.translatable("options.sounds");
    MsiButton msi_btn = null;
    Button sound_btn = null;

    for (GuiEventListener child : screen.children()) {
      if (child instanceof MsiButton btn)
        msi_btn = btn;
      else if (child instanceof Button btn && btn.getMessage().getString().equals(sound_text.getString()))
        sound_btn = btn;
    }

    if (sound_btn != null && msi_btn != null)
      msi_btn.setPosition(sound_btn.getX() + sound_btn.getWidth() + 2, sound_btn.getY());
  }
}
