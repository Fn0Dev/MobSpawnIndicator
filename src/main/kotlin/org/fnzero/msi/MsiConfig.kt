package org.fnzero.msi

import com.electronwill.nightconfig.core.file.CommentedFileConfig
import java.nio.file.Files
import java.nio.file.Path

/**
 * TOML config shared by both loaders, stored at `config/msi-common.toml`
 * with the same keys the mod has always used.
 *
 * [init] is called by the loader entry with its config directory
 * (NeoForge: `FMLPaths.CONFIGDIR`, Fabric: `FabricLoader#getConfigDir`).
 */
object MsiConfig {
  private const val FILE_NAME = "msi-common.toml"

  private const val RADIUS_DEFAULT = 16
  private const val RADIUS_MIN = 4
  private const val RADIUS_MAX = 64
  private const val OPACITY_DEFAULT = 0.5
  private const val OPACITY_MIN = 0.05
  private const val OPACITY_MAX = 1.0

  var render_rad: Int = RADIUS_DEFAULT
    internal set
  var show_danger: Boolean = true
    internal set
  var show_warn: Boolean = true
    internal set
  var show_safe: Boolean = false
    internal set
  var overlay_opacity: Double = OPACITY_DEFAULT
    internal set

  var loaded = false
    private set

  private lateinit var config_dir: Path

  fun init(dir: Path) {
    config_dir = dir
    load()
  }

  fun load() {
    val file = config_dir.resolve(FILE_NAME)
    if (Files.exists(file)) {
      CommentedFileConfig.builder(file).build().use { cfg ->
        cfg.load()
        render_rad = cfg.getIntOrElse("renderRadius", RADIUS_DEFAULT).coerceIn(RADIUS_MIN, RADIUS_MAX)
        show_danger = cfg.getOrElse("showDanger", true)
        show_warn = cfg.getOrElse("showWarning", true)
        show_safe = cfg.getOrElse("showSafe", false)
        overlay_opacity =
          cfg.getOrElse("overlayOpacity", OPACITY_DEFAULT).coerceIn(OPACITY_MIN, OPACITY_MAX)
      }
    }
    loaded = true
  }

  fun save() {
    if (!loaded) return
    val file = config_dir.resolve(FILE_NAME)
    Files.createDirectories(file.parent)
    CommentedFileConfig.builder(file).build().use { cfg ->
      cfg.load()
      cfg.set<Int>("renderRadius", render_rad)
      cfg.setComment("renderRadius", "Radius in blocks to check around the player")
      cfg.set<Boolean>("showDanger", show_danger)
      cfg.setComment("showDanger", "Show overlay on blocks where mobs CAN spawn (light = 0)")
      cfg.set<Boolean>("showWarning", show_warn)
      cfg.setComment("showWarning", "Show overlay on blocks where light = 1 (barely safe, warning)")
      cfg.set<Boolean>("showSafe", show_safe)
      cfg.setComment("showSafe", "Show overlay on fully safe blocks (light >= 2)")
      cfg.set<Double>("overlayOpacity", overlay_opacity)
      cfg.setComment(
        "overlayOpacity",
        "Transparency of the overlay (0.0 = invisible, 1.0 = fully opaque)",
      )
      cfg.save()
    }
  }

  /** Restores factory defaults and persists them. */
  fun reset_defaults() {
    render_rad = RADIUS_DEFAULT
    show_danger = true
    show_warn = true
    show_safe = false
    overlay_opacity = OPACITY_DEFAULT
    save()
  }
}
