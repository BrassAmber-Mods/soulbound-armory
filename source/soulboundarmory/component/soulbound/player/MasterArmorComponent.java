package soulboundarmory.component.soulbound.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import soulboundarmory.component.Components;
import soulboundarmory.component.soulbound.item.armor.HelmetComponent;
import soulboundarmory.item.SoulboundArmorItem;
import soulboundarmory.module.component.EntityComponentKey;

public class MasterArmorComponent extends MasterComponent<MasterArmorComponent> {
	public MasterArmorComponent(PlayerEntity player) {
		super(player);

		this.boundSlot = -1;
		this.store(new HelmetComponent(this));
	}

	@Override public EntityComponentKey<MasterArmorComponent> key() {
		return Components.armor;
	}

	@Override public boolean matches(ItemStack stack) {
		return stack.getItem() instanceof SoulboundArmorItem;
	}
}
