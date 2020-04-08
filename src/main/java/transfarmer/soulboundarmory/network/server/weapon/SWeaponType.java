package transfarmer.soulboundarmory.network.server.weapon;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import transfarmer.soulboundarmory.capability.SoulItemHelper;
import transfarmer.soulboundarmory.capability.weapon.ISoulWeapon;
import transfarmer.soulboundarmory.capability.weapon.SoulWeaponProvider;
import transfarmer.soulboundarmory.item.ItemSoulWeapon;
import transfarmer.soulboundarmory.statistics.SoulType;
import transfarmer.soulboundarmory.statistics.weapon.SoulWeaponType;

public class SWeaponType implements IMessage {
    private int index;

    public SWeaponType() {
        this.index = -1;
    }

    public SWeaponType(final SoulType type) {
        this.index = type.getIndex();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.index = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(index);
    }

    public static final class Handler implements IMessageHandler<SWeaponType, IMessage> {
        @Override
        public IMessage onMessage(SWeaponType message, MessageContext context) {
            final SoulType type = SoulWeaponType.get(message.index);
            final EntityPlayerMP player = context.getServerHandler().player;
            final ISoulWeapon instance = SoulWeaponProvider.get(player);
            int slot = instance.getBoundSlot();
            instance.setCurrentType(type);

            if (!SoulItemHelper.hasSoulWeapon(player)) {
                player.inventory.clearMatchingItems(Items.WOODEN_SWORD, -1, 37, null);
            } else {
                SoulItemHelper.removeSoulWeapons(player);
            }

            if (slot >= 0) {
                final ItemStack boundSlotItem = player.inventory.getStackInSlot(slot);

                if (boundSlotItem.getItem() != Items.AIR && !(boundSlotItem.getItem() instanceof ItemSoulWeapon)) {
                    slot = -1;
                } else if (boundSlotItem.getItem() instanceof ItemSoulWeapon) {
                    player.inventory.deleteStack(boundSlotItem);
                }
            }

            if (slot < 0) {
                slot = player.inventory.getFirstEmptyStack();
            }

            player.inventory.setInventorySlotContents(slot, new ItemStack(instance.getCurrentType().getItem()));

            return null;
        }
    }
}
