package soulboundarmory.component.soulbound.item.weapon;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraftforge.common.Tags;
import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.component.soulbound.player.MasterComponent;
import soulboundarmory.component.statistics.StatisticType;
import soulboundarmory.config.Configuration;
import soulboundarmory.skill.Skills;
import soulboundarmory.util.Math2;
import soulboundarmory.util.Util;
import soulboundarmory.util.Util2;

import java.util.List;

public abstract class WeaponComponent<T extends ItemComponent<T>> extends ItemComponent<T> {
	protected double criticalHitProgress;

	public WeaponComponent(MasterComponent<?> component) {
		super(component);

		this.statistics.statistics(StatisticType.efficiency, StatisticType.criticalHitRate);
		this.addSkills(Skills.enderPull);
	}

	/**
	 @return true if the hit is critical
	 */
	public boolean hit() {
		this.criticalHitProgress += this.doubleValue(StatisticType.criticalHitRate);

		if (this.criticalHitProgress >= 1) {
			this.criticalHitProgress--;
			return true;
		}

		return false;
	}

	@Override public int levelXP(int level) {
		return this.canLevelUp()
			? Configuration.initialWeaponXP + 3 * Math2.iround(Math.pow(level, 1.65))
			: -1;
	}

	@Override public void killed(LivingEntity entity) {
		if (this.isServer()) {
			var damage = Util.attribute(entity, EntityAttributes.GENERIC_ATTACK_DAMAGE);
			var speed = Util.attribute(entity, EntityAttributes.GENERIC_ATTACK_SPEED);
			var difficulty = this.player.world.getDifficulty().getId();

			this.add(StatisticType.experience, entity.getMaxHealth()
				* (difficulty == 0 ? Configuration.Multipliers.peaceful : difficulty) * Configuration.Multipliers.difficulty
				* (1 + Util.attribute(entity, EntityAttributes.GENERIC_ARMOR) * Configuration.Multipliers.armor)
				* (damage <= 0 ? Configuration.Multipliers.passive : 1 + damage * Configuration.Multipliers.attackDamage)
				* (1 + speed * Configuration.Multipliers.attackSpeed)
				* (entity.getType().isIn(Tags.EntityTypes.BOSSES) ? Configuration.Multipliers.boss : 1)
				* (this.player.world.getServer().isHardcore() ? Configuration.Multipliers.hardcore : 1)
				* (damage > 0 && entity.isBaby() ? Configuration.Multipliers.hostileBaby : 1)
			);
		}
	}

	@Override public double attributeTotal(StatisticType attribute) {
		if (attribute == StatisticType.efficiency && this.statistic(StatisticType.efficiency).min() == 0) {
			return this.doubleValue(attribute) == 0 ? 0 : super.attributeTotal(attribute) - this.increase(attribute);
		}

		return super.attributeTotal(attribute);
	}

	@Override public List<StatisticType> screenAttributes() {
		return ReferenceArrayList.of(StatisticType.attackDamage, StatisticType.attackSpeed, StatisticType.criticalHitRate);
	}

	@Override public List<MutableText> tooltip() {
		var tooltip = Util2.list(StatisticType.attackDamage, StatisticType.attackSpeed);

		if (this.criticalHitRate() > 0) tooltip.add(StatisticType.criticalHitRate);
		if (this.efficiency() > 0) tooltip.add(StatisticType.efficiency);

		return tooltip.stream().map(this::formatTooltip).toList();
	}

	@Override public void serialize(NbtCompound tag) {
		super.serialize(tag);

		tag.putDouble("criticalHitProgress", this.criticalHitProgress);
	}

	@Override public void deserialize(NbtCompound tag) {
		super.deserialize(tag);

		this.criticalHitProgress = tag.getDouble("criticalHitProgress");
	}
}
