package soulboundarmory.component.soulbound.item.armor;

import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.component.soulbound.player.MasterArmorComponent;
import soulboundarmory.component.statistics.StatisticType;
import soulboundarmory.skill.Skills;

public class ChestplateComponent extends ArmorComponent<ChestplateComponent> {
	public ChestplateComponent(MasterArmorComponent component) {
		super(component);

		this.addSkills(Skills.climbingClaws);
	}

	@Override public ItemComponentType<ChestplateComponent> type() {
		return ItemComponentType.chestplate;
	}

	@Override public double increase(StatisticType type) {
		return type == StatisticType.armor ? 8D / 75 : super.increase(type);
	}
}
