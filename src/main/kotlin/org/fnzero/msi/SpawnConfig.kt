package org.fnzero.msi

import net.neoforged.neoforge.common.ModConfigSpec

class SpawnConfig private constructor(builder: ModConfigSpec.Builder) {
  val render_rad: ModConfigSpec.IntValue =
    builder
      .comment("Radius in blocks to check around the player")
      .defineInRange("renderRadius", 16, 4, 64)

  val show_danger: ModConfigSpec.BooleanValue =
    builder
      .comment("Show overlay on blocks where mobs CAN spawn (light = 0)")
      .define("showDanger", true)

  val show_warn: ModConfigSpec.BooleanValue =
    builder
      .comment("Show overlay on blocks where light = 1 (barely safe, warning)")
      .define("showWarning", true)

  val show_safe: ModConfigSpec.BooleanValue =
    builder.comment("Show overlay on fully safe blocks (light >= 2)").define("showSafe", false)

  val overlay_opacity: ModConfigSpec.DoubleValue =
    builder
      .comment("Transparency of the overlay (0.0 = invisible, 1.0 = fully opaque)")
      .defineInRange("overlayOpacity", 0.5, 0.05, 1.0)

  companion object {
    val COMMON_SPEC: ModConfigSpec
    val COMMON: SpawnConfig

    init {
      val builder = ModConfigSpec.Builder()
      val config = SpawnConfig(builder)
      COMMON_SPEC = builder.build()
      COMMON = config
    }
  }
}
