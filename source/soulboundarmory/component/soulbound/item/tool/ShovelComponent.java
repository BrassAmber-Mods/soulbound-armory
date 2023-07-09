package soulboundarmory.component.soulbound.item.tool;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolActions;
import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.component.soulbound.player.MasterComponent;
import soulboundarmory.component.statistics.StatisticType;

public class ShovelComponent extends ToolComponent<ShovelComponent> {
	public ShovelComponent(MasterComponent<?> component) {
		super(component);

		this.statistics
			.constant(2, StatisticType.attackDamage)
			.constant(1, StatisticType.attackSpeed);
	}

	@Override public ItemComponentType<ShovelComponent> type() {
		return ItemComponentType.shovel;
	}

	@Override protected boolean canAbsorb(ItemStack stack) {
		return stack.canPerformAction(ToolActions.SHOVEL_DIG);
	}
}
