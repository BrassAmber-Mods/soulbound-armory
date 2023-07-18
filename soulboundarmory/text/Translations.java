package soulboundarmory.text;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraftforge.common.TierSortingRegistry;
import soulboundarmory.SoulboundArmory;
import soulboundarmory.skill.Skill;
import soulboundarmory.util.Util2;

import java.util.List;
import java.util.stream.Stream;

public interface Translations {
	MutableText empty = Text.empty();

	MutableText toolSelection = of("selection").text();
	MutableText buttonAttributes = of("attributes").text();
	MutableText buttonEnchantments = of("enchantments").text();
	MutableText skills = of("skills").text();
	MutableText bind = of("bind").text();
	MutableText unbind = of("unbind").text();
	PluralizableTranslation unspentPoints = pluralizable(of("point"), of("points"));
	PluralizableTranslation upgradeCost = pluralizable(of("upgrade_cost_singular"), of("upgrade_cost_plural"));
	PluralizableTranslation learnCost = pluralizable(of("learn_cost_singular"), of("learn_cost_plural"));
	MutableText red = of("red").text();
	MutableText green = of("green").text();
	MutableText blue = of("blue").text();
	MutableText alpha = of("alpha").text();
	Translation barLevel = of("bar.level");
	Translation barXP = of("bar.xp");
	Translation barFullXP = of("bar.full_xp");
	Translation style = of("style");
	MutableText xpStyle = of("style.experience").text();
	MutableText bossStyle = of("style.boss").text();
	MutableText horseStyle = of("style.horse").text();
	MutableText configure = of("configure").text();

	Translation level = display("level");
	Translation finiteLevel = of("finite_level");

	Translation levelupMessage = of("levelup");
	MutableText cannotAbsorbDamaged = of("cannot_absorb_damaged").text();
	MutableText cannotAbsorbWeaker = of("cannot_absorb_weaker").text();

	Translation tier = of("tier");
	Translation tooltipTier = tooltip("tier");
	Translation noItem = of("no_item");

	static Translation of(String path) {
		return Translation.of("%s:%s", SoulboundArmory.ID, path);
	}

	static Translation of(String category, String path) {
		return Translation.of("%s:%s.%s", SoulboundArmory.ID, category, path);
	}

	static PluralizableTranslation pluralizable(Translation singular, Translation plural) {
		return new PluralizableTranslation(singular, plural);
	}

	static Text skillName(Skill skill) {
		return Text.of(Util2.capitalize(I18n.translate("%s:skill.name.%s".formatted(skill.id().getNamespace(), skill.id().getPath()))));
	}

	static Text toolMaterial(ToolMaterial material) {
		return of("tool_material", TierSortingRegistry.getName(material).getPath()).text();
	}

	static List<Text> skillDescription(Skill skill) {
		return Stream.of(I18n.translate("%s:skill.desc.%s".formatted(skill.id().getNamespace(), skill.id().getPath())).split("\n")).map(Text::of).toList();
	}

	static Translation tooltip(String path) {
		return of("tooltip", path);
	}

	static Translation display(String path) {
		return of("display", path);
	}
}
