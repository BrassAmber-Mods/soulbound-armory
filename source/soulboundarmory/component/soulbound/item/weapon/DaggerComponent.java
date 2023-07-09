package soulboundarmory.component.soulbound.item.weapon;

import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.component.soulbound.player.MasterComponent;
import soulboundarmory.component.statistics.StatisticType;
import soulboundarmory.skill.Skills;
import soulboundarmory.util.Util2;

import java.util.List;

public class DaggerComponent extends WeaponComponent<DaggerComponent> {
	public DaggerComponent(MasterComponent<?> component) {
		super(component);

		this.statistics
			.constant(2, StatisticType.reach)
			.min(2, StatisticType.attackSpeed, StatisticType.attackDamage);

		this.addSkills(Skills.circumspection, Skills.precision, Skills.nourishment, Skills.shadowClone, Skills.returne, Skills.sneakReturn);
	}

	@Override public ItemComponentType<DaggerComponent> type() {
		return ItemComponentType.dagger;
	}

	@Override public List<StatisticType> screenAttributes() {
		return Util2.add(super.screenAttributes(), StatisticType.efficiency);
	}

	@Override public double increase(StatisticType type) {
		if (type == StatisticType.attackSpeed) return 0.04;
		if (type == StatisticType.attackDamage) return 0.05;
		if (type == StatisticType.criticalHitRate) return 0.02;
		if (type == StatisticType.efficiency) return 0.06;

		return 0;
	}
}
