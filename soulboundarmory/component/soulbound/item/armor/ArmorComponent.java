package soulboundarmory.component.soulbound.item.armor;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.text.MutableText;
import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.component.soulbound.player.MasterArmorComponent;
import soulboundarmory.component.statistics.StatisticType;
import soulboundarmory.config.Configuration;
import soulboundarmory.util.Math2;
import soulboundarmory.util.Util2;

import java.util.List;

public abstract class ArmorComponent<T extends ItemComponent<T>> extends ItemComponent<T> {
	public ArmorComponent(MasterArmorComponent component) {
		super(component);

		this.statistics.statistics(StatisticType.armor, StatisticType.toughness, StatisticType.knockbackResistance);
	}

	@Override public double increase(StatisticType type) {
		return type == StatisticType.toughness ? 3D / 20
			: type == StatisticType.knockbackResistance ? 1D / 20
			: 0;
	}

	@Override public int levelXP(int level) {
		return this.canLevelUp()
			? Configuration.initialArmorXP + 3 * Math2.iround(Math.pow(level, 1.25))
			: -1;
	}

	@Override public void tookDamage(float damage) {
		if (this.isServer()) {
			this.add(StatisticType.experience, damage);
		}
	}

	@Override public List<StatisticType> screenAttributes() {
		return ReferenceArrayList.of(StatisticType.armor, StatisticType.toughness, StatisticType.knockbackResistance);
	}

	@Override public List<MutableText> tooltip() {
		var tooltip = Util2.list(StatisticType.armor);

		if (this.doubleValue(StatisticType.toughness) > 0) tooltip.add(StatisticType.toughness);
		if (this.doubleValue(StatisticType.knockbackResistance) > 0) tooltip.add(StatisticType.knockbackResistance);

		return tooltip.stream().map(this::formatTooltip).toList();
	}
}
