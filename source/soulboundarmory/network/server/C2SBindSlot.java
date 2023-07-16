package soulboundarmory.network.server;

import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.network.ExtendedPacketBuffer;
import soulboundarmory.network.ItemComponentPacket;
import soulboundarmory.network.Packets;

public final class C2SBindSlot extends ItemComponentPacket {
	@Override public void execute(ItemComponent<?> component) {
		var slot = this.message.readInt();
		component.boundSlot = component.boundSlot == slot ? -1 : slot;
		Packets.clientBindSlot.send(component.player, new ExtendedPacketBuffer(component).writeInt(component.boundSlot));
	}
}
