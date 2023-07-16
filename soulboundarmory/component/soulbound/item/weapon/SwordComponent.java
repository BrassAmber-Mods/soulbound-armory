package soulboundarmory.component.soulbound.item.weapon;

import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.component.soulbound.player.MasterComponent;
import soulboundarmory.component.statistics.StatisticType;
import soulboundarmory.skill.Skills;
import soulboundarmory.util.Util2;

import java.util.List;

public class SwordComponent extends WeaponComponent<SwordComponent> {
	public SwordComponent(MasterComponent<?> component) {
		super(component);

		this.statistics
			.constant(3, StatisticType.reach)
			.min(1.6, StatisticType.attackSpeed)
			.min(3, StatisticType.attackDamage);

		this.addSkills(Skills.circumspection, Skills.precision, Skills.nourishment);
	}

	@Override public ItemComponentType<SwordComponent> type() {
		return ItemComponentType.sword;
	}

	@Override public void attributeModifiers(Multimap<EntityAttribute, EntityAttributeModifier> modifiers, EquipmentSlot slot) {
		if (slot == EquipmentSlot.MAINHAND) {
			modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, this.modifier(Item.ATTACK_DAMAGE_MODIFIER_ID, StatisticType.attackDamage));
			modifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED, this.modifier(Item.ATTACK_SPEED_MODIFIER_ID, StatisticType.attackSpeed));
		}
	}

	@Override public List<StatisticType> screenAttributes() {
		return Util2.add(super.screenAttributes(), StatisticType.efficiency);
	}

	@Override public double increase(StatisticType type) {
		if (type == StatisticType.attackSpeed) return 0.03;
		if (type == StatisticType.attackDamage) return 0.07;
		if (type == StatisticType.criticalHitRate) return 0.015;
		if (type == StatisticType.efficiency) return 0.04;

		return 0;
	}
}
