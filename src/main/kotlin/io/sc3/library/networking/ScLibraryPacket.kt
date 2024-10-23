package io.sc3.library.networking

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.createC2SPacket
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.createS2CPacket
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.CustomPayload
import net.minecraft.network.packet.Packet
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

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

fun <T: ScLibraryPacket> registerClientReceiver(id: CustomPayload.Id<CustomPayload>, factory: (buf: CustomPayload) -> T) {
  ClientPlayNetworking.registerGlobalReceiver(id) { payload, ctx ->
    val packet = factory(payload)
    packet.onClientReceive(payload, ctx)
  }
}

fun <T: ScLibraryPacket> registerServerReceiver(id: CustomPayload.Id<CustomPayload>, factory: (buf: CustomPayload) -> T) {
  ServerPlayNetworking.registerGlobalReceiver(id) { payload, ctx ->
    val packet = factory(payload)
    packet.onServerReceive(payload, ctx)
  }
}

abstract class ScLibraryPacket {
  abstract val id: CustomPayload.Id<CustomPayload>

  abstract val payload: CustomPayload;
  
  fun toC2SPacket(): Packet<*> = createC2SPacket(payload)
  fun toS2CPacket(): Packet<*> = createS2CPacket(payload)

  open fun onClientReceive(payload: CustomPayload, ctx: ClientPlayNetworking.Context) {}

  open fun onServerReceive(payload: CustomPayload, ctx: ServerPlayNetworking.Context) {}
}
