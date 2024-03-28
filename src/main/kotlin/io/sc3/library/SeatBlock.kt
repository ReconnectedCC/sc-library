package io.sc3.library

import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

interface SeatBlock {
  fun canSitOn(
    world: World,
    pos: BlockPos,
    state: BlockState,
    hitResult: BlockHitResult?,
    player: PlayerEntity?,
  ): ActionResult
    = ActionResult.PASS

  fun getSeatPos(
    world: World,
    pos: BlockPos,
    state: BlockState,
  ): Vec3d
    = pos.toCenterPos()
}
