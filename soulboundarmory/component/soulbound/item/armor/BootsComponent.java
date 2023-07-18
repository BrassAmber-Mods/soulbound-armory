package soulboundarmory.component.soulbound.item.armor;

import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.component.soulbound.player.MasterArmorComponent;
import soulboundarmory.component.statistics.StatisticType;
import soulboundarmory.skill.Skills;

public class BootsComponent extends ArmorComponent<BootsComponent> {
	public BootsComponent(MasterArmorComponent component) {
		super(component);

		this.addSkills(Skills.shoeSpikes);
	}

	@Override public ItemComponentType<BootsComponent> type() {
		return ItemComponentType.boots;
	}

	@Override public double increase(StatisticType type) {
		return type == StatisticType.armor ? 3D / 75 : super.increase(type);
	}
}
