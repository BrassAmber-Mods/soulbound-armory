package soulboundarmory.component.statistics;

import net.minecraft.text.MutableText;
import net.minecraftforge.registries.IForgeRegistry;
import soulboundarmory.text.Translations;
import soulboundarmory.module.transform.Register;
import soulboundarmory.module.transform.RegisterAll;
import soulboundarmory.module.transform.Registry;
import soulboundarmory.registry.Identifiable;
import soulboundarmory.text.Translation;

import java.util.function.Consumer;

@RegisterAll(type = StatisticType.class, registry = "statistic")
public class StatisticType extends Identifiable {
	@Register("attribute_points") public static final StatisticType attributePoints = new StatisticType(Category.datum);
	@Register("enchantment_points") public static final StatisticType enchantmentPoints = new StatisticType(Category.datum);
	@Register("xp") public static final StatisticType experience = new StatisticType(Category.datum);
	@Register("level") public static final StatisticType level = new StatisticType(Category.datum);
	@Register("skill_points") public static final StatisticType skillPoints = new StatisticType(Category.datum);
	@Register("upgrade_progress") public static final StatisticType upgradeProgress = new StatisticType(Category.datum);

	@Register("attack_speed") public static final StatisticType attackSpeed = new StatisticType(Category.attribute, statistic -> statistic.defaultMax(4));
	@Register("attack_damage") public static final StatisticType attackDamage = new StatisticType(Category.attribute);
	@Register("critical_hit_rate") public static final StatisticType criticalHitRate = new StatisticType(Category.attribute, statistic -> statistic.defaultMax(1));
	@Register("efficiency") public static final StatisticType efficiency = new StatisticType(Category.attribute);
	@Register("reach") public static final StatisticType reach = new StatisticType(Category.attribute);
	@Register("armor") public static final StatisticType armor = new StatisticType(Category.attribute);
	@Register("toughness") public static final StatisticType toughness = new StatisticType(Category.attribute);
	@Register("knockback_resistance") public static final StatisticType knockbackResistance = new StatisticType(Category.attribute);

	public final Category category;

	private final Consumer<Statistic> initialize;

	@Registry("statistic") public static native IForgeRegistry<StatisticType> registry();

	public StatisticType(Category category, Consumer<Statistic> initialize) {
		this.category = category;
		this.initialize = initialize;
	}

	public StatisticType(Category category) {
		this(category, statistic -> {});
	}

	public final Statistic instantiate() {
		var statistic = new Statistic(this);
		this.initialize.accept(statistic);

		return statistic;
	}

	public Translation displayTranslation() {
		return Translations.display(registry().getKey(this).getPath());
	}

	public MutableText display(Object... arguments) {
		return this.displayTranslation().text(arguments);
	}

	public MutableText tooltip(Object... arguments) {
		return Translations.tooltip(registry().getKey(this).getPath()).translate(arguments);
	}

	@Override public String toString() {
		return "statistic type " + this.id();
	}
}
