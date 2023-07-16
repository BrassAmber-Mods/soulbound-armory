package soulboundarmory.component.soulbound.item.tool;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolActions;
import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.component.soulbound.player.MasterComponent;
import soulboundarmory.component.statistics.StatisticType;

public class AxeComponent extends ToolComponent<AxeComponent> {
	public AxeComponent(MasterComponent<?> component) {
		super(component);

		this.statistics
			.constant(6, StatisticType.attackDamage)
			.constant(0.8, StatisticType.attackSpeed);
	}

	@Override public ItemComponentType<AxeComponent> type() {
		return ItemComponentType.axe;
	}

	@Override protected boolean canAbsorb(ItemStack stack) {
		return stack.canPerformAction(ToolActions.AXE_DIG);
	}
}
