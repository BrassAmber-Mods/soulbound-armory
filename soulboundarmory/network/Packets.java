package soulboundarmory.network;

import soulboundarmory.SoulboundArmory;
import soulboundarmory.network.client.*;
import soulboundarmory.network.server.*;
import soulboundarmory.util.Util2;

public final class Packets {
	public static final PacketKey.Server<ExtendedPacketBuffer, C2SAttribute> serverAttribute = server();
	public static final PacketKey.Server<ExtendedPacketBuffer, C2SBindSlot> serverBindSlot = server();
	public static final PacketKey.Server<ExtendedPacketBuffer, C2SConfig> serverConfig = server();
	public static final PacketKey.Server<ExtendedPacketBuffer, C2SEnchant> serverEnchant = server();
	public static final PacketKey.Server<ExtendedPacketBuffer, C2SSelectItem> serverSelectItem = server();
	public static final PacketKey.Server<ExtendedPacketBuffer, C2SReset> serverReset = server();
	public static final PacketKey.Server<ExtendedPacketBuffer, C2SSkill> serverSkill = server();
	public static final PacketKey.Server<ExtendedPacketBuffer, C2STab> serverTab = server();
	public static final PacketKey.Server<ExtendedPacketBuffer, C2SClimb> serverClimb = server();

	public static final PacketKey.Client<ExtendedPacketBuffer, S2CBindSlot> clientBindSlot = client();
	public static final PacketKey.Client<ExtendedPacketBuffer, S2CCriticalHitParticles> clientCriticalHitParticles = client();
	public static final PacketKey.Client<ExtendedPacketBuffer, S2CEnchant> clientEnchant = client();
	public static final PacketKey.Client<ExtendedPacketBuffer, S2CFreeze> clientFreeze = client();
	public static final PacketKey.Client<ExtendedPacketBuffer, S2CRefresh> clientRefresh = client();
	public static final PacketKey.Client<ExtendedPacketBuffer, S2CSync> clientSync = client();
	public static final PacketKey.Client<ExtendedPacketBuffer, S2CSyncItem> clientSyncItem = client();
	public static final PacketKey.Client<ExtendedPacketBuffer, S2CUnlock> clientUnlock = client();

	private static byte id;

	private static <T, P extends Packet<T>, K extends PacketKey<T, P>> K register(K key) {
		SoulboundArmory.channel.registerMessage(
			id++,
			key.type,
			P::write,
			buffer -> {
				var packet = key.instantiate();
				packet.read(buffer);

				return packet;
			},
			(packet, context) -> {
				context.get().enqueueWork(() -> packet.execute(context.get()));
				context.get().setPacketHandled(true);
			}
		);

		return key;
	}

	private static <T, P extends Packet<T>> PacketKey.Server<T, P> server(P... dummy) {
		return register(new PacketKey.Server<>(Util2.componentType(dummy)));
	}

	private static <T, P extends Packet<T>> PacketKey.Client<T, P> client(P... dummy) {
		return register(new PacketKey.Client<>(Util2.componentType(dummy)));
	}
}
