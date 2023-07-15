package soulboundarmory.component.soulbound.item.armor;

import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.component.soulbound.player.MasterArmorComponent;
import soulboundarmory.component.statistics.StatisticType;

public class HelmetComponent extends ArmorComponent<HelmetComponent> {
	public HelmetComponent(MasterArmorComponent component) {
		super(component);
	}

	@Override public ItemComponentType<HelmetComponent> type() {
		return ItemComponentType.helmet;
	}

	@Override public double increase(StatisticType type) {
		return type == StatisticType.armor ? 3D / 75
			: type == StatisticType.toughness ? 3D / 20
			: type == StatisticType.knockbackResistance ? 1D / 20
			: 0;
	}
}
