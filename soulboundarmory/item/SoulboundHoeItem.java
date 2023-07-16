package soulboundarmory.item;

import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import soulboundarmory.component.soulbound.item.ItemComponentType;

public class SoulboundHoeItem extends HoeItem implements SoulboundToolItem {
	public SoulboundHoeItem() {
		super(SoulboundItems.baseMaterial, 0, -3, new Settings().group(ItemGroup.TOOLS));
	}

	@Override public ActionResult useOnBlock(ItemUsageContext context) {
		return this.useOnBlock(context, super.useOnBlock(context), ItemComponentType.hoe);
	}
}
