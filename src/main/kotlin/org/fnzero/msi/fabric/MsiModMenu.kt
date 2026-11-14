//? if fabric {
package org.fnzero.msi.fabric

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.screens.Screen
import org.fnzero.msi.client.gui.SpawnSettingsScreen

class MsiModMenu : ModMenuApi {
  override fun getModConfigScreenFactory(): ConfigScreenFactory<*> =
    ConfigScreenFactory<Screen> { parent -> SpawnSettingsScreen(parent) }
}
//?}
