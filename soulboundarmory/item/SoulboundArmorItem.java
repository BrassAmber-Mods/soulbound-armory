package soulboundarmory.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import soulboundarmory.component.soulbound.item.ItemComponent;

public class SoulboundArmorItem extends ArmorItem implements SoulboundItem {
	public SoulboundArmorItem(EquipmentSlot slot) {
		super(Material.INSTANCE, slot, new Settings().group(ItemGroup.COMBAT));
	}

	@Override public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return "soulboundarmory:textures/armor/%s_layer_%s.png".formatted(
			ItemComponent.of(entity, stack)
				.map(component -> component.level() >= 100 ? 100 : component.level() >= 50 ? 50 : 0)
				.orElse(0),
			slot == EquipmentSlot.LEGS ? 2 : 1
		);
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
			return "soulboundarmory:soulbound";
		}

		@Override public float getToughness() {
			return 0;
		}

		@Override public float getKnockbackResistance() {
			return 0;
		}
	}
}
