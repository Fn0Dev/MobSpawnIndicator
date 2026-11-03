//? if fabric {
//? if >=1.21.9 {
package org.fnzero.msi.fabric.mixin

//? if >=26.1 {
import com.mojang.blaze3d.textures.GpuSampler
//?}
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup
import org.fnzero.msi.SpawnRenderer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

/**
 * Draws the spawn overlay right after translucent chunk sections are rendered,
 * standing in for fabric-api's world-render events, which fabric-api does not
 * ship on 1.21.9/1.21.10 (16.0/16.1) and 26.1 (23.x). Targets are string-based
 * so the mixin compiles without importing the target class.
 */
@Mixin(targets = ["net.minecraft.client.renderer.chunk.ChunkSectionsToRender"])
class ChunkTranslucentRenderMixin {
  //? if >=26.1 {
  @Inject(
    method = ["renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/blaze3d/textures/GpuSampler;)V"],
    at = [At("TAIL")],
  )
  fun msi_after_group(group: ChunkSectionLayerGroup, sampler: GpuSampler, ci: CallbackInfo) {
    if (group != ChunkSectionLayerGroup.TRANSLUCENT) return
    SpawnRenderer.fabric_after_translucent()
  }
  //?} else {
  @Inject(
    method = ["renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;)V"],
    at = [At("TAIL")],
  )
  fun msi_after_group(group: ChunkSectionLayerGroup, ci: CallbackInfo) {
    if (group != ChunkSectionLayerGroup.TRANSLUCENT) return
    SpawnRenderer.fabric_after_translucent()
  }
  //?}
}
//?}
//?}
