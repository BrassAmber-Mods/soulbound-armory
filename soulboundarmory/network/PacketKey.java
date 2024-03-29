package soulboundarmory.network;

import net.auoeke.reflect.Constructors;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import soulboundarmory.SoulboundArmory;
import soulboundarmory.util.Util;

import java.util.function.Supplier;

/**
 A key to a registered packet type; used for sending packets.

 @param <T> the message type of {@link P}
 @param <P> the packet type to which this key corresponds
 */
public abstract sealed class PacketKey<T, P extends Packet<T>> permits PacketKey.Client, PacketKey.Server {
	public final Class<P> type;

	PacketKey(Class<P> type) {
		this.type = type;
	}

	final P store(T message) {
		var packet = this.instantiate();
		packet.message = message;

		return packet;
	}

	/**
	 Instantiate a packet of the registered type.
	 */
	final P instantiate() {
		return Constructors.instantiate(this.type);
	}

	/**
	 A server-to-client packet key.
	 */
	public static final class Client<T, P extends Packet<T>> extends PacketKey<T, P> {
		Client(Class<P> type) {
			super(type);
		}

		/**
		 Send a message from the server to a client.
		 */
		public void send(Entity player, T message) {
			SoulboundArmory.channel.sendTo(this.store(message), ((ServerPlayerEntity) player).networkHandler.connection, NetworkDirection.PLAY_TO_CLIENT);
		}

		public void sendIfServer(Entity player, Supplier<T> message) {
			if (!player.world.isClient) {
				this.send(player, message.get());
			}
		}

		public void sendNearby(Entity entity, T message) {
			SoulboundArmory.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), this.store(message));
		}

		public void sendNearbyIfServer(Entity entity, Supplier<T> message) {
			if (!entity.world.isClient) {
				this.sendNearby(entity, message.get());
			}
		}
	}

	/**
	 A client-to-server packet key.
	 */
	public static final class Server<T, P extends Packet<T>> extends PacketKey<T, P> {
		Server(Class<P> type) {
			super(type);
		}

		/**
		 Send a message from the client to the server.
		 */
		public void send(T message) {
			SoulboundArmory.channel.sendToServer(this.store(message));
		}

		public void sendIfClient(Supplier<T> message) {
			if (Util.isClient()) {
				this.send(message.get());
			}
		}
	}
}
