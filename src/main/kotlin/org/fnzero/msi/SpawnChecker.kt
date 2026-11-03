package org.fnzero.msi

import net.minecraft.core.BlockPos
//? if >=26.2 {
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
//?}
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import net.minecraft.world.level.LightLayer
import net.minecraft.world.level.NaturalSpawner
import net.minecraft.world.phys.Vec3

enum class SpawnLevel {
  RED,
  YELLOW,
  GREEN,
}

data class SpawnCheckResult(val pos: BlockPos, val level: SpawnLevel)

object SpawnChecker {
  // 26.x registry-fied entity types; earlier versions expose the constant directly.
  //? if >=26.2 {
  private val ZOMBIE: EntityType<*> =
    BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("zombie"))
  //?} else {
  private val ZOMBIE: EntityType<*> = EntityType.ZOMBIE
  //?}

  fun get_spawn_pos(
    level: Level,
    player_pos: Vec3,
    radius: Int,
    show_danger: Boolean,
    show_warn: Boolean,
    show_safe: Boolean,
  ): List<SpawnCheckResult> {
    val results = mutableListOf<SpawnCheckResult>()
    val center_x = player_pos.x.toInt()
    val center_y = player_pos.y.toInt()
    val center_z = player_pos.z.toInt()

    for (x in center_x - radius..center_x + radius) {
      for (z in center_z - radius..center_z + radius) {
        for (y in center_y + 2 downTo center_y - 24) {
          val pos = BlockPos(x, y, z)
          val result = check_pos(level, pos, show_danger, show_warn, show_safe)
          if (result != null) results.add(result)
        }
      }
    }

    return results
  }

  private fun check_pos(
    level: Level,
    pos: BlockPos,
    show_danger: Boolean,
    show_warn: Boolean,
    show_safe: Boolean,
  ): SpawnCheckResult? {
    val ground = level.getBlockState(pos)
    if (!ground.isValidSpawn(level, pos, ZOMBIE)) return null

    val spawn_pos = pos.above()
    val spawn_state = level.getBlockState(spawn_pos)
    if (
      !NaturalSpawner.isValidEmptySpawnBlock(
        level,
        spawn_pos,
        spawn_state,
        spawn_state.fluidState,
        ZOMBIE,
      )
    )
      return null

    val head_pos = spawn_pos.above()
    val head_state = level.getBlockState(head_pos)
    if (
      !NaturalSpawner.isValidEmptySpawnBlock(
        level,
        head_pos,
        head_state,
        head_state.fluidState,
        ZOMBIE,
      )
    )
      return null

    val block_light = level.getBrightness(LightLayer.BLOCK, spawn_pos)

    return when (block_light) {
      0 -> if (show_danger) SpawnCheckResult(pos, SpawnLevel.RED) else null
      1 -> if (show_warn) SpawnCheckResult(pos, SpawnLevel.YELLOW) else null
      else -> if (show_safe) SpawnCheckResult(pos, SpawnLevel.GREEN) else null
    }
  }
}
