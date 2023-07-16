package soulboundarmory.network.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.network.ItemComponentPacket;

/**
 A server-to-client packet that is sent to update the client's bound slot.
 <br><br>
 buffer: <br>
 - Identifier (item component type) <br>
 - int (slot) <br>
 */
public final class S2CBindSlot extends ItemComponentPacket {
	@OnlyIn(Dist.CLIENT)
	@Override protected void execute(ItemComponent<?> component) {
		component.boundSlot = this.message.readInt();
	}
}
