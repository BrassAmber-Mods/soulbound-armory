package soulboundarmory.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SwordItem;

public abstract class SoulboundMeleeWeapon extends SwordItem implements SoulboundWeaponItem {
    protected final float reach;
    protected final float attackSpeed;

    public SoulboundMeleeWeapon(int attackDamage, float attackSpeed, float reach) {
        super(SoulboundToolMaterial.SOULBOUND, attackDamage, attackSpeed, new Properties().group(ItemGroup.COMBAT));

        this.reach = reach;
        this.attackSpeed = attackSpeed;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot) {
        var modifiers = HashMultimap.<Attribute, AttributeModifier>create();

        if (slot == EquipmentSlotType.MAINHAND) {
            modifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", this.attackSpeed, AttributeModifier.Operation.ADDITION));
            modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", this.getAttackDamage(), AttributeModifier.Operation.ADDITION));
        }

        return modifiers;
    }
}