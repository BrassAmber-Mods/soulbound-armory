package user11681.soulboundarmory.network.C2S;

import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.util.Identifier;
import user11681.soulboundarmory.network.common.ExtendedPacketBuffer;
import user11681.soulboundarmory.network.common.ItemComponentPacket;
import user11681.soulboundarmory.component.statistics.Category;

public class C2SReset extends ItemComponentPacket {
    @Override
    protected void accept(final PacketContext context, final ExtendedPacketBuffer buffer) {
        final Identifier identifier = buffer.readIdentifier();

        if (identifier != null) {
            final Category category = Category.valueOf(identifier);

            component.reset(category);
        } else {
            component.reset();
        }

//        component.sync();
        component.refresh();
    }
}