package soulboundarmory.component.soulbound.item.armor;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import net.minecraft.text.MutableText;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import soulboundarmory.client.gui.screen.*;
import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.component.soulbound.player.MasterComponent;
import soulboundarmory.component.statistics.StatisticType;
import soulboundarmory.config.Configuration;
import soulboundarmory.util.Util2;

import java.util.List;

public abstract class ArmorComponent<T extends ItemComponent<T>> extends ItemComponent<T> {
	public ArmorComponent(MasterComponent<?> component) {
		super(component);

		this.statistics.statistics(StatisticType.armor, StatisticType.toughness, StatisticType.knockbackResistance);
	}

	@Override public int levelXP(int level) {
		return this.canLevelUp()
			? Configuration.initialArmorXP + 3 * (int) Math.round(Math.pow(level, 1.25))
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

	@OnlyIn(Dist.CLIENT)
	@Override public List<Tab> tabs() {
		return ReferenceList.of(new SelectionTab(), new AttributeTab(), new EnchantmentTab());
	}
}
