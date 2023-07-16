package soulboundarmory.item;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.component.statistics.StatisticType;

public interface SoulboundToolItem extends SoulboundItem {
	default ActionResult useOnBlock(ItemUsageContext context, ActionResult result, ItemComponentType<?> type) {
		if (result.isAccepted()) {
			type.nullable(context.getPlayer()).ifPresent(component -> {
				component.add(StatisticType.experience, 1);
			});
		}

		return result;
	}
}
