package soulboundarmory.component.soulbound.item.armor;

import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.component.soulbound.player.MasterArmorComponent;
import soulboundarmory.component.statistics.StatisticType;

public class LeggingsComponent extends ArmorComponent<LeggingsComponent> {
	public LeggingsComponent(MasterArmorComponent component) {
		super(component);
	}

	@Override public ItemComponentType<LeggingsComponent> type() {
		return ItemComponentType.leggings;
	}

	@Override public double increase(StatisticType type) {
		return type == StatisticType.armor ? 6D / 75 : super.increase(type);
	}
}
