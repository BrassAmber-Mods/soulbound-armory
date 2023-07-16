package soulboundarmory.config;

import com.google.common.base.Functions;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import soulboundarmory.client.gui.bar.BarStyle;
import soulboundarmory.client.gui.widget.SelectionEntryWidget;
import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.module.config.*;
import soulboundarmory.util.Math2;

import java.util.Map;
import java.util.stream.Collectors;

@ConfigurationFile
public final class Configuration {
	@Comment("The number of experience points required to reach the first level for armor.")
	public static int initialArmorXP = 16;

	@Comment("The number of experience points required to reach the first level for tools.")
	public static int initialToolXP = 16;

	@Comment("The number of experience points required to reach the first level for weapons.")
	public static int initialWeaponXP = 64;

	@Comment("The number of experience levels required per enchantment point.")
	public static int levelsPerEnchantment = 10;

	@Comment("The number of experience levels required per skill point.")
	public static int levelsPerSkillPoint = 5;

	@Comment({
		"The maximum level.",
		"maxLevel < 0 => no limit."
	})
	public static int maxLevel = -1;

	@Comment("The minimum level wherefrom soulbound items are preserved after death.")
	public static int preservationLevel = 0;

	@Comment("Point restoration does not cost experience levels.")
	public static boolean freeRestoration = true;

	// @Comment({
	// 	"Allow items added by this mod to be modified externally.",
	// 	"If false, then the mod will replace such modified items by their unmodified versions when it detects them.",
	// 	"Example: the mod detects a soulbound sword enchanted by a command and replaces it by a soulbound sword with the statistics and enchantments as displayed in the menu."
	// })
	// public static boolean externalModification;

	@Flat
	@Category("items")
	public static class Items {
		public static Map<String, Boolean> enabled = new Object2BooleanLinkedOpenHashMap<>(
			ItemComponentType.registry().getKeys().stream().collect(Collectors.toMap(Identifier::getPath, Functions.constant(true)))
		);

		@Name("dagger")
		public static class Dagger {
			public static double throwSpeedFactor = 1;
		}

		@Name("big sword")
		public static class Bigsword {
			public static double fluidChargeAccelerationFactor = 1;
		}
	}

	@Flat
	@Category("experience multipliers")
	public static class Multipliers {
		@Comment("1 + value * armor")
		public static double armor = 0.2;

		@Comment("1 + value * damage")
		public static double attackDamage = 0.35;

		@Comment("1 + value * speed")
		public static double attackSpeed = 0.5;

		@Comment({
			"value * difficulty",
			"peaceful = 0; hard = 3"
		})
		public static double difficulty = 0.5;

		public static double passive = 0;
		public static double hostileBaby = 2;
		public static double boss = 3;
		public static double peaceful = 0;
		public static double hardcore = 2;
	}

	@OnlyIn(Dist.CLIENT)
	@Flat
	@Category("client")
	public static class Client {
		@Comment("Receive levelup notifications above the hotbar.")
		public static boolean levelupNotifications;

		@Comment("Display option button and sliders in the menu.")
		public static boolean displayOptions;

		@Comment("Display an experience bar for the currently held soulbound item.")
		public static boolean overlayExperienceBar = true;

		@Comment("Enable enchantment glint.")
		public static boolean enchantmentGlint;

		// @Comment("Display attributes in tooltips.")
		// public static boolean tooltipAttributes = true;

		@Comment({
			"The style of selection entries.",
			"ICON: white (locked) or yellow (unlocked) advancement box with item's icon.",
			"TEXT: button with item's name."
		})
		public static SelectionEntryWidget.Type selectionEntryType = SelectionEntryWidget.Type.ICON;

		@Name("experience bar")
		public static class Bar {
			public static BarStyle style = BarStyle.EXPERIENCE;

			@Interval(max = 255)
			public static int red = 160;

			@Interval(max = 255)
			public static int green = 255;

			@Interval(max = 255)
			public static int blue = 160;

			@Interval(max = 255)
			public static int alpha = 255;

			public static void set(int id, int value) {
				switch (id) {
					case 0 -> red = value;
					case 1 -> green = value;
					case 2 -> blue = value;
					case 3 -> alpha = value;
					default -> throw new IllegalArgumentException("color component ID: " + id);
				}
			}

			public static int get(int id) {
				return switch (id) {
					case 0 -> red;
					case 1 -> green;
					case 2 -> blue;
					case 3 -> alpha;
					default -> throw new IllegalArgumentException("color component ID: " + id);
				};
			}

			public static float getf(int id) {
				return get(id) / 255F;
			}

			public static int argb() {
				return Math2.pack(red, green, blue, alpha);
			}
		}
	}
}
