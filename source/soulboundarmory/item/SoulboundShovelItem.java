package soulboundarmory.item;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ShovelItem;

public class SoulboundShovelItem extends ShovelItem implements SoulboundToolItem {
	public SoulboundShovelItem() {
		super(SoulboundItems.baseMaterial, 0, -3, new Settings().group(ItemGroup.TOOLS));
	}
}
