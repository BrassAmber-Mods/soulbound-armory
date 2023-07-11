package soulboundarmory.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemGroup;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class SoulboundArmorItem extends ArmorItem implements SoulboundItem {
	public SoulboundArmorItem(EquipmentSlot slot) {
		super(Material.INSTANCE, slot, new Settings().group(ItemGroup.COMBAT));
	}

	public enum Material implements ArmorMaterial {
		INSTANCE;

		@Override public int getDurability(EquipmentSlot slot) {
			return 0;
		}

		@Override public int getProtectionAmount(EquipmentSlot slot) {
			return 0;
		}

		@Override public int getEnchantability() {
			return 0;
		}

		@Override public SoundEvent getEquipSound() {
			return SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE;
		}

		@Override public Ingredient getRepairIngredient() {
			return Ingredient.empty();
		}

		@Override public String getName() {
			return "soulbound";
		}

		@Override public float getToughness() {
			return 0;
		}

		@Override public float getKnockbackResistance() {
			return 0;
		}
	}
}
