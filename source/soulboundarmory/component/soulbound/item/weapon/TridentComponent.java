package soulboundarmory.component.soulbound.item.weapon;

import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.component.soulbound.player.MasterComponent;
import soulboundarmory.component.statistics.StatisticType;
import soulboundarmory.skill.Skills;

public class TridentComponent extends WeaponComponent<TridentComponent> {
	public TridentComponent(MasterComponent<?> component) {
		super(component);

		this.statistics
			.constant(3, StatisticType.reach)
			.min(1.1, StatisticType.attackSpeed)
			.min(5, StatisticType.attackDamage);

		this.addSkills(Skills.circumspection, Skills.precision, Skills.nourishment);
	}

	@Override public ItemComponentType<TridentComponent> type() {
		return ItemComponentType.trident;
	}

	@Override public double increase(StatisticType type) {
		if (type == StatisticType.attackDamage) return 0.15;
		if (type == StatisticType.attackSpeed) return 0.03;
		if (type == StatisticType.criticalHitRate) return 0.005;

		return 0;
	}
}
