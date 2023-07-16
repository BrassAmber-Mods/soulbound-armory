package soulboundarmory.component.soulbound.item.tool;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.TierSortingRegistry;
import soulboundarmory.text.Translations;
import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.component.soulbound.player.MasterComponent;
import soulboundarmory.component.statistics.StatisticType;
import soulboundarmory.config.Configuration;
import soulboundarmory.item.SoulboundItems;
import soulboundarmory.skill.Skills;
import soulboundarmory.util.Math2;

import java.util.List;
import java.util.Objects;

public abstract class ToolComponent<T extends ItemComponent<T>> extends ItemComponent<T> {
	public ToolMaterial nextMaterial;
	protected ToolMaterial material = ToolMaterials.WOOD;

	public ToolComponent(MasterComponent<?> component) {
		super(component);

		this.statistics
			.statistics(StatisticType.efficiency)
			.min(2, StatisticType.reach)
			.max(0, StatisticType.upgradeProgress);

		this.addSkills(Skills.absorption, Skills.circumspection, Skills.enderPull);
	}

	public Text materialName() {
		return Translations.toolMaterial(this.material);
	}

	public void absorb() {
		var stack = this.player.getOffHandStack();

		if (stack.getItem() instanceof ToolItem tool && this.canAbsorb(stack)) {
			var tiers = TierSortingRegistry.getSortedTiers();

			if (tiers.indexOf(this.nextMaterial == null ? this.material : this.nextMaterial) >= tiers.indexOf(tool.getMaterial())) {
				this.player.sendMessage(Translations.cannotAbsorbWeaker, true);
			} else if (stack.isDamaged()) {
				this.player.sendMessage(Translations.cannotAbsorbDamaged, true);
			} else {
				stack.decrement(1);
				this.nextMaterial = tool.getMaterial();
				this.statistic(StatisticType.upgradeProgress).max(1000);
				this.synchronize();
			}
		}
	}

	@Override public int levelXP(int level) {
		return this.canLevelUp()
			? Configuration.initialToolXP + Math2.iround(4 * Math.pow(level, 1.25))
			: -1;
	}

	@Override public void mined(BlockState state, BlockPos position) {
		if (this.isServer() && this.itemStack.isSuitableFor(state)) {
			var hardness = state.getHardness(this.player.world, position);

			if (hardness > 0) {
				var delta = Math.min(1, state.calcBlockBreakingDelta(this.player, this.player.world, position));
				var xp = hardness + 2 * (1 - delta);
				xp = delta == 1 ? Math.min(10, xp) : xp;
				this.add(StatisticType.experience, xp);
			}
		}
	}

	@Override public ToolMaterial material() {
		return SoulboundItems.material(this.material);
	}

	@Override public void add(StatisticType type, double amount) {
		if (type == StatisticType.experience && amount > 0) {
			var upgrade = this.statistic(StatisticType.upgradeProgress);
			upgrade.add(amount);

			if (this.nextMaterial != null && upgrade.intValue() == upgrade.max()) {
				upgrade.setToMin();
				upgrade.max(0);
				this.material = this.nextMaterial;
				this.nextMaterial = null;
			}
		}

		super.add(type, amount);
	}

	@Override public void reset() {
		super.reset();

		this.nextMaterial = null;
		this.material = ToolMaterials.WOOD;

		this.synchronize();
	}

	@Override public double increase(StatisticType type) {
		return type == StatisticType.efficiency ? 0.5
			: type == StatisticType.reach ? 0.1
			: 0;
	}

	@Override public MutableText format(StatisticType statistic) {
		if (statistic == StatisticType.upgradeProgress) {
			return this.nextMaterial == null ? Translations.tier.text(this.materialName())
				: StatisticType.upgradeProgress.displayTranslation().translate(this.materialName(), Translations.toolMaterial(this.nextMaterial), this.formatValue(statistic));
		}

		return super.format(statistic);
	}

	@Override public List<StatisticType> screenAttributes() {
		return ReferenceArrayList.of(StatisticType.efficiency, StatisticType.reach);
	}

	@Override public List<MutableText> tooltip() {
		return List.of(
			this.formatTooltip(StatisticType.reach),
			this.formatTooltip(StatisticType.efficiency),
			this.nextMaterial == null ? Translations.tooltipTier.translate(this.materialName()) : StatisticType.upgradeProgress.tooltip(this.formatValue(StatisticType.upgradeProgress), this.materialName(), Translations.toolMaterial(this.nextMaterial))
		);
	}

	@Override public void serialize(NbtCompound tag) {
		super.serialize(tag);

		if (this.material != null) {
			tag.putString("material", TierSortingRegistry.getName(this.material).toString());
		}

		if (this.nextMaterial != null) {
			tag.putString("nextMaterial", TierSortingRegistry.getName(this.nextMaterial).toString());
		}
	}

	@Override public void deserialize(NbtCompound tag) {
		super.deserialize(tag);

		this.material = Objects.requireNonNullElse(TierSortingRegistry.byName(new Identifier(tag.getString("material"))), this.material);
		this.nextMaterial = TierSortingRegistry.byName(new Identifier(tag.getString("nextMaterial")));
	}

	protected abstract boolean canAbsorb(ItemStack stack);
}
