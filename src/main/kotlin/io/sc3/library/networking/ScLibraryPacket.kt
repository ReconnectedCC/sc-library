package io.sc3.library.networking

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnReason
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos


inline fun <reified T> event(noinline invokerFactory: (Array<T>) -> T): Event<T>
  = EventFactory.createArrayBacked(T::class.java, invokerFactory)

inline fun <reified T> clientPacketEvent() = event<(packet: T) -> Unit> { cb ->
  { packet -> cb.forEach { it(packet) } }
}

inline fun <reified T> serverPacketEvent() = event<(packet: T, player: ServerPlayerEntity,
                                                    handler: ServerPlayNetworkHandler,
                                                    responseSender: PacketSender) -> Unit> { cb ->
  { packet, player, handler, responseSender -> cb.forEach { it(packet, player, handler, responseSender) } }
}

fun <T : ScLibraryPacket> registerServerReceiver(
  id: CustomPayload.Id<T>
) {
  ServerPlayNetworking.registerGlobalReceiver(id) { payload, ctx ->
    payload.onServerReceive(ctx)
  }
}

fun <T : ScLibraryPacket> registerClientReceiver(
  id: CustomPayload.Id<T>
) {
  ClientPlayNetworking.registerGlobalReceiver(id) { payload, ctx ->
    payload.onClientReceive(ctx)
  }
}

abstract class ScLibraryPacket : CustomPayload {
  abstract fun onServerReceive(ctx: ServerPlayNetworking.Context)
  abstract fun onClientReceive(ctx: ClientPlayNetworking.Context)
}
