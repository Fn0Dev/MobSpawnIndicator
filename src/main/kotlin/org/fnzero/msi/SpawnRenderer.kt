package org.fnzero.msi

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.MultiBufferSource
//? if >=1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderTypes
//?} else {
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
//?}
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f

object SpawnRenderer {
  private val RED = floatArrayOf(1.0f, 0.2f, 0.2f)
  private val YELLOW = floatArrayOf(1.0f, 1.0f, 0.2f)
  private val GREEN = floatArrayOf(0.2f, 1.0f, 0.2f)

  //? if >=1.21.5 {
  //? if >=1.21.11 {
  private val OVERLAY_RENDER_TYPE = RenderTypes.debugQuads()
  //?} else {
  private val OVERLAY_RENDER_TYPE = RenderType.debugQuads()
  //?}
  //?} else {
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
  //?}

  private var pos_cache: List<SpawnCheckResult> = emptyList()
  private var last_calc_tick = 0
  private const val CALC_INTERVAL = 5

  fun camera_pos(mc: Minecraft): Vec3 {
    val camera = mc.gameRenderer.mainCamera
    //? if >=1.21.11 {
    return camera.position()
    //?} else {
    return camera.position
    //?}
  }

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

  //? if >=1.21.9 {
  fun fabric_after_translucent() {
    if (!MsiState.enabled) return
    if (!MsiConfig.loaded) return
    if (pos_cache.isEmpty()) return

    val mc = Minecraft.getInstance()
    val cam = camera_pos(mc)
    val matrix = Matrix4f()
    val alpha = MsiConfig.overlay_opacity.toFloat()

    val buff_src = mc.renderBuffers().bufferSource()
    val buffer = buff_src.getBuffer(OVERLAY_RENDER_TYPE)

    for (result in pos_cache) {
      val rgb =
        when (result.level) {
          SpawnLevel.RED -> RED
          SpawnLevel.YELLOW -> YELLOW
          SpawnLevel.GREEN -> GREEN
        }

      val x = result.pos.x.toDouble() - cam.x
      val y = result.pos.y.toDouble() + 1.0 - cam.y
      val z = result.pos.z.toDouble() - cam.z

      draw_quad(buffer, matrix, x, y, z, rgb, alpha)
    }

    buff_src.endBatch(OVERLAY_RENDER_TYPE)
  }
  //?}

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

    val cam_x = camera_pos.x
    val cam_y = camera_pos.y
    val cam_z = camera_pos.z

    val buffer = buff_src.getBuffer(OVERLAY_RENDER_TYPE)

    for (result in pos_cache) {
      val rgb =
        when (result.level) {
          SpawnLevel.RED -> RED
          SpawnLevel.YELLOW -> YELLOW
          SpawnLevel.GREEN -> GREEN
        }

      val x = result.pos.x.toDouble() - cam_x
      val y = result.pos.y.toDouble() + 1.0 - cam_y
      val z = result.pos.z.toDouble() - cam_z

      draw_quad(buffer, pose_stack.last().pose(), x, y, z, rgb, alpha)
    }

    pose_stack.popPose()
    buff_src.endBatch(OVERLAY_RENDER_TYPE)
  }

  private fun draw_quad(
    buffer: VertexConsumer,
    matrix: org.joml.Matrix4f,
    x: Double,
    y: Double,
    z: Double,
    rgb: FloatArray,
    alpha: Float,
  ) {
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
