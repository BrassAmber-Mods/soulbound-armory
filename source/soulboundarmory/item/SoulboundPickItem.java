package soulboundarmory.item;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.PickaxeItem;

public class SoulboundPickItem extends PickaxeItem implements SoulboundToolItem {
	public SoulboundPickItem() {
		super(SoulboundItems.baseMaterial, 0, -2.4F, new Settings().group(ItemGroup.TOOLS));
	}
}
