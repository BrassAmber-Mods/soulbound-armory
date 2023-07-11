package soulboundarmory.component.soulbound.item.armor;

import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.component.soulbound.player.MasterComponent;
import soulboundarmory.component.statistics.StatisticType;

public class HelmetComponent extends ArmorComponent<HelmetComponent> {
	public HelmetComponent(MasterComponent<?> component) {
		super(component);
	}

	@Override public ItemComponentType<HelmetComponent> type() {
		return ItemComponentType.helmet;
	}

	@Override public double increase(StatisticType type) {
		return type == StatisticType.armor ? 5D / 75
			: type == StatisticType.toughness ? 3D / 20
			: type == StatisticType.knockbackResistance ? 1D / 20
			: 0;
	}
}
