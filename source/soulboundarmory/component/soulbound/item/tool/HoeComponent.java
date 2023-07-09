package soulboundarmory.component.soulbound.item.tool;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolActions;
import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.component.soulbound.player.MasterComponent;
import soulboundarmory.component.statistics.StatisticType;

public class HoeComponent extends ToolComponent<HoeComponent> {
	public HoeComponent(MasterComponent<?> component) {
		super(component);

		this.statistics
			.constant(0, StatisticType.attackDamage)
			.constant(1, StatisticType.attackSpeed);
	}

	@Override public ItemComponentType<HoeComponent> type() {
		return ItemComponentType.hoe;
	}

	@Override protected boolean canAbsorb(ItemStack stack) {
		return stack.canPerformAction(ToolActions.HOE_DIG);
	}
}
