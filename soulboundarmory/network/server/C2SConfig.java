package soulboundarmory.network.server;

import soulboundarmory.component.Components;
import soulboundarmory.network.BufferPacket;

/**
 A client-to-server packet containing the client's configuration.
 <p>
 buffer: <br>
 - boolean (levelup notifications)
 */
public final class C2SConfig extends BufferPacket {
	@Override public void execute() {
		var component = Components.config.of(this.player());
		component.levelupNotifications = this.message.readBoolean();
	}
}
