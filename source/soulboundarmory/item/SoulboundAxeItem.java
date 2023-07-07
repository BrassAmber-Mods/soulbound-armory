package soulboundarmory.item;

import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemGroup;

public class SoulboundAxeItem extends AxeItem implements SoulboundToolItem {
	public SoulboundAxeItem() {
		super(SoulboundItems.baseMaterial, 0, -3.2F, new Settings().group(ItemGroup.TOOLS));
	}
}
