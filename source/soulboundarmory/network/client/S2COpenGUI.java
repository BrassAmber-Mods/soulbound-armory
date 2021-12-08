package soulboundarmory.network.client;

import soulboundarmory.SoulboundArmoryClient;
import soulboundarmory.client.gui.screen.SoulboundTab;
import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.network.ItemComponentPacket;

public final class S2COpenGUI extends ItemComponentPacket {
    @Override
    public void execute(ItemComponent<?> storage) {
        if (SoulboundArmoryClient.client.currentScreen instanceof SoulboundTab) {
            // storage.openGUI(this.message.readInt());
        }
    }
}