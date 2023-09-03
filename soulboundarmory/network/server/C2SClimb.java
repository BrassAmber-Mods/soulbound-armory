package soulboundarmory.network.server;

import soulboundarmory.component.Components;
import soulboundarmory.network.BufferPacket;

public final class C2SClimb extends BufferPacket {
	@Override protected void execute() {
		Components.armor.of(this.player()).climbing = this.message.readByte();
	}
}
