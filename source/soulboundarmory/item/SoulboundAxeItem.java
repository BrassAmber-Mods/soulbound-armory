package soulboundarmory.item;

import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import soulboundarmory.component.soulbound.item.ItemComponentType;

public class SoulboundAxeItem extends AxeItem implements SoulboundToolItem {
	public SoulboundAxeItem() {
		super(SoulboundItems.baseMaterial, 0, -3.2F, new Settings().group(ItemGroup.TOOLS));
	}

	@Override public ActionResult useOnBlock(ItemUsageContext context) {
		return this.useOnBlock(context, super.useOnBlock(context), ItemComponentType.axe);
	}
}
