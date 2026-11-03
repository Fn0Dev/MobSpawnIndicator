package org.fnzero.msi

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.phys.Vec3

/**
 * Loader-agnostic overlay renderer: loaders call [client_tick] from their tick
 * event and [render_overlay] from their world-render event.
 */
object SpawnRenderer {
  private val RED = floatArrayOf(1.0f, 0.2f, 0.2f)
  private val YELLOW = floatArrayOf(1.0f, 1.0f, 0.2f)
  private val GREEN = floatArrayOf(0.2f, 1.0f, 0.2f)

  private val OVERLAY_RENDER_TYPE: RenderType =
    RenderType.create(
      "msi_spawn_overlay",
      DefaultVertexFormat.POSITION_COLOR,
      VertexFormat.Mode.QUADS,
      1536,
      false,
      true,
      RenderType.CompositeState.builder()
        .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
        .setCullState(RenderStateShard.NO_CULL)
        .createCompositeState(false),
    )

  private var pos_cache: List<SpawnCheckResult> = emptyList()
  private var last_calc_tick = 0
  private const val CALC_INTERVAL = 5

  fun client_tick() {
    if (!MsiState.enabled) return
    if (!MsiConfig.loaded) return

    val mc = Minecraft.getInstance()
    val player = mc.player ?: return
    val level = mc.level ?: return

    val curr_tick = level.gameTime.toInt()
    if (curr_tick - last_calc_tick < CALC_INTERVAL) return
    last_calc_tick = curr_tick

    pos_cache =
      SpawnChecker.get_spawn_pos(
        level,
        player.position(),
        MsiConfig.render_rad,
        MsiConfig.show_danger,
        MsiConfig.show_warn,
        MsiConfig.show_safe,
      )
  }

  fun render_overlay(
    pose_stack: PoseStack,
    camera_pos: Vec3,
    buff_src: MultiBufferSource.BufferSource,
  ) {
    if (!MsiState.enabled) return
    if (!MsiConfig.loaded) return
    if (pos_cache.isEmpty()) return

    val alpha = MsiConfig.overlay_opacity.toFloat()

    pose_stack.pushPose()

    val camX = camera_pos.x
    val camY = camera_pos.y
    val camZ = camera_pos.z

    val buffer = buff_src.getBuffer(OVERLAY_RENDER_TYPE)

    for (result in pos_cache) {
      val rgb =
        when (result.level) {
          SpawnLevel.RED -> RED
          SpawnLevel.YELLOW -> YELLOW
          SpawnLevel.GREEN -> GREEN
        }

      val x = result.pos.x.toDouble() - camX
      val y = result.pos.y.toDouble() + 1.0 - camY
      val z = result.pos.z.toDouble() - camZ

      add_quad(buffer, pose_stack, x, y, z, rgb, alpha)
    }

    pose_stack.popPose()
    buff_src.endBatch(OVERLAY_RENDER_TYPE)
  }

  private fun add_quad(
    buffer: VertexConsumer,
    pose_stack: PoseStack,
    x: Double,
    y: Double,
    z: Double,
    rgb: FloatArray,
    alpha: Float,
  ) {
    val matrix = pose_stack.last().pose()

    val x0 = x.toFloat()
    val y0 = y.toFloat() + 0.002f
    val z0 = z.toFloat()
    val x1 = x0 + 1.0f
    val z1 = z0 + 1.0f

    buffer.addVertex(matrix, x0, y0, z0).setColor(rgb[0], rgb[1], rgb[2], alpha)
    buffer.addVertex(matrix, x1, y0, z0).setColor(rgb[0], rgb[1], rgb[2], alpha)
    buffer.addVertex(matrix, x1, y0, z1).setColor(rgb[0], rgb[1], rgb[2], alpha)
    buffer.addVertex(matrix, x0, y0, z1).setColor(rgb[0], rgb[1], rgb[2], alpha)
  }
}
