//? if neoforge {
package org.fnzero.msi.neoforge

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import net.neoforged.neoforge.common.NeoForge
import org.fnzero.msi.MsiConfig
import org.fnzero.msi.MsiState
import org.fnzero.msi.client.gui.SpawnSettingsScreen

@Mod(MsiState.MOD_ID)
class MobSpawnIndicator(mod_event_bus: IEventBus, mod_container: ModContainer) {
  init {
    MsiConfig.init(FMLPaths.CONFIGDIR.get())

    // Config button in the NeoForge mods list; Catalogue picks this up too
    mod_container.registerExtensionPoint(
      IConfigScreenFactory::class.java,
      IConfigScreenFactory { _, parent -> SpawnSettingsScreen(parent) },
    )

    mod_event_bus.register(NeoForgeEvents)
    NeoForge.EVENT_BUS.register(NeoForgeEvents)
  }
}
//?}
