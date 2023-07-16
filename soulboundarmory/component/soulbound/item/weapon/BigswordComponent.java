package soulboundarmory.component.soulbound.item.weapon;

import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.component.soulbound.player.MasterComponent;
import soulboundarmory.component.statistics.StatisticType;
import soulboundarmory.skill.Skills;

public class BigswordComponent extends WeaponComponent<BigswordComponent> {
	private int chargeDelay;

	public BigswordComponent(MasterComponent<?> component) {
		super(component);

		this.statistics
			.min(4, StatisticType.attackDamage)
			.min(1, StatisticType.attackSpeed)
			.min(3, StatisticType.reach);

		this.addSkills(Skills.circumspection, Skills.precision, Skills.nourishment);
	}

	@Override public ItemComponentType<BigswordComponent> type() {
		return ItemComponentType.bigsword;
	}

	@Override public double increase(StatisticType type) {
		if (type == StatisticType.attackDamage) return 0.12;
		if (type == StatisticType.attackSpeed) return 0.025;
		if (type == StatisticType.criticalHitRate) return 0.008;
		if (type == StatisticType.efficiency) return 0.03;

		return 0;
	}

	@Override public void tick() {
		super.tick();

		this.chargeDelay--;
	}

	public void charge() {
		this.chargeDelay = 16;
	}

	public boolean canCharge() {
		return this.chargeDelay <= 0;
	}
}
