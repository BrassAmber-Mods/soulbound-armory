package soulboundarmory.component.soulbound.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import soulboundarmory.component.Components;
import soulboundarmory.component.soulbound.item.tool.AxeComponent;
import soulboundarmory.component.soulbound.item.tool.HoeComponent;
import soulboundarmory.component.soulbound.item.tool.PickaxeComponent;
import soulboundarmory.component.soulbound.item.tool.ShovelComponent;
import soulboundarmory.item.SoulboundToolItem;
import soulboundarmory.module.component.EntityComponentKey;

public class MasterToolComponent extends MasterComponent<MasterToolComponent> {
	public MasterToolComponent(PlayerEntity player) {
		super(player);

		this.store(new PickaxeComponent(this), new AxeComponent(this), new ShovelComponent(this), new HoeComponent(this));
	}

	@Override public EntityComponentKey<MasterToolComponent> key() {
		return Components.tool;
	}

	@Override public boolean matches(ItemStack stack) {
		return stack.getItem() instanceof SoulboundToolItem;
	}
}
