package transfarmer.soulboundarmory.network.server.tool;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import transfarmer.soulboundarmory.capability.soulbound.tool.SoulToolProvider;

public class C2SToolTab implements IMessage {
    int tab;

    public C2SToolTab() {}

    public C2SToolTab(final int tab) {
        this.tab = tab;
    }

    public void fromBytes(final ByteBuf buffer) {
        this.tab = buffer.readInt();
    }

    public void toBytes(final ByteBuf buffer) {
        buffer.writeInt(this.tab);
    }

    public static final class Handler implements IMessageHandler<C2SToolTab, IMessage> {
        @Override
        public IMessage onMessage(C2SToolTab message, MessageContext context) {
            SoulToolProvider.get(context.getServerHandler().player).setCurrentTab(message.tab);

            return null;
        }
    }
}
