package soulboundarmory.item;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.ActionResult;
import soulboundarmory.component.soulbound.item.ItemComponentType;

public class SoulboundShovelItem extends ShovelItem implements SoulboundToolItem {
	public SoulboundShovelItem() {
		super(SoulboundItems.baseMaterial, 0, -3, new Settings().group(ItemGroup.TOOLS));
	}

	@Override public ActionResult useOnBlock(ItemUsageContext context) {
		return this.useOnBlock(context, super.useOnBlock(context), ItemComponentType.shovel);
	}
}
