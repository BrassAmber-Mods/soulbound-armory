package soulboundarmory.component.soulbound.item.tool;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolActions;
import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.component.soulbound.player.MasterComponent;
import soulboundarmory.component.statistics.StatisticType;

public class PickaxeComponent extends ToolComponent<PickaxeComponent> {
	public PickaxeComponent(MasterComponent<?> component) {
		super(component);

		this.statistics
			.constant(2, StatisticType.attackDamage)
			.constant(1.2, StatisticType.attackSpeed);
	}

	@Override public ItemComponentType<PickaxeComponent> type() {
		return ItemComponentType.pickaxe;
	}

	@Override protected boolean canAbsorb(ItemStack stack) {
		return stack.canPerformAction(ToolActions.PICKAXE_DIG);
	}
}
