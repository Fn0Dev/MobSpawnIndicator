//? if fabric {
package org.fnzero.msi.fabric

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.screens.Screen
import org.fnzero.msi.client.gui.SpawnSettingsScreen

/**
 * Only loaded by Fabric Loader when ModMenu is installed (declared under the
 * "modmenu" entrypoint), so referencing ModMenu classes here is safe.
 */
class MsiModMenu : ModMenuApi {
  override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
    return ConfigScreenFactory<Screen> { parent -> SpawnSettingsScreen(parent) }
  }
}
//?}
