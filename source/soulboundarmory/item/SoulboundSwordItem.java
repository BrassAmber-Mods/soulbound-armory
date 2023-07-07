package soulboundarmory.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class SoulboundSwordItem extends SoulboundMeleeWeapon {
	public SoulboundSwordItem() {
		super(3, -2.4F, 0);
	}

	@Override public UseAction getUseAction(ItemStack stack) {
		return UseAction.BLOCK;
	}

	@Override public int getMaxUseTime(ItemStack stack) {
		return 72000;
	}

	@Override public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		player.setCurrentHand(hand);
		return TypedActionResult.consume(player.getStackInHand(hand));
	}

	@Override public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
		return ToolActions.DEFAULT_SHIELD_ACTIONS.contains(toolAction);
	}
}
