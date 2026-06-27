package org.fnzero.msi

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.RenderLevelStageEvent

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

  @SubscribeEvent
  fun on_client_tick(event: ClientTickEvent.Pre) {
    if (!MobSpawnIndicator.enabled) return
    if (!SpawnConfig.COMMON_SPEC.isLoaded) return

    val mc = Minecraft.getInstance()
    val player = mc.player ?: return
    val level = mc.level ?: return

    val curr_tick = level.gameTime.toInt()
    if (curr_tick - last_calc_tick < CALC_INTERVAL) return
    last_calc_tick = curr_tick

    val config = SpawnConfig.COMMON
    pos_cache =
      SpawnChecker.get_spawn_pos(
        level,
        player.position(),
        config.render_rad.get(),
        config.show_danger.get(),
        config.show_warn.get(),
        config.show_safe.get(),
      )
  }

  @SubscribeEvent
  fun on_render(event: RenderLevelStageEvent) {
    if (event.stage != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return
    if (!MobSpawnIndicator.enabled) return
    if (!SpawnConfig.COMMON_SPEC.isLoaded) return
    if (pos_cache.isEmpty()) return

    val mc = Minecraft.getInstance()
    val player = mc.player ?: return

    val alpha = SpawnConfig.COMMON.overlay_opacity.get().toFloat()

    val camera = mc.gameRenderer.mainCamera
    val cam_pos = camera.position

    val pose_stack = event.poseStack
    val buff_src = mc.renderBuffers().bufferSource()

    pose_stack.pushPose()

    val camX = cam_pos.x
    val camY = cam_pos.y
    val camZ = cam_pos.z

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
