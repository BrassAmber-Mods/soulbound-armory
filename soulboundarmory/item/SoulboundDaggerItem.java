package soulboundarmory.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolAction;
import soulboundarmory.component.Components;
import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.component.statistics.StatisticType;
import soulboundarmory.config.Configuration;
import soulboundarmory.entity.SoulboundDaggerEntity;

public class SoulboundDaggerItem extends SoulboundMeleeWeapon {
	private static final int USE_TIME = 1200;

	public SoulboundDaggerItem() {
		super(1, -2, -1);
	}

	private static double damageRatio(double attackSpeed, int timeLeft) {
		return Math.min(1, attackSpeed * (USE_TIME - timeLeft) / 40F);
	}

	@Override public int getMaxUseTime(ItemStack stack) {
		return USE_TIME;
	}

	@Override public UseAction getUseAction(ItemStack stack) {
		return UseAction.SPEAR;
	}

	@Override public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		user.setCurrentHand(hand);
		return ActionResult.CONSUME;
	}

	@Override public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		player.setCurrentHand(hand);
		return TypedActionResult.consume(player.getStackInHand(hand));
	}

	@Override public void onStoppedUsing(ItemStack itemStack, World world, LivingEntity entity, int timeLeft) {
		if (entity instanceof ServerPlayerEntity player) {
			var attackSpeed = ItemComponentType.dagger.of(player).attributeTotal(StatisticType.attackSpeed);
			var damageRatio = damageRatio(attackSpeed, timeLeft);
			world.spawnEntity(new SoulboundDaggerEntity(player, false, damageRatio * attackSpeed * Configuration.Items.Dagger.throwSpeedFactor, damageRatio));

			if (!player.isCreative()) {
				player.getInventory().removeOne(itemStack);
			}
		}
	}

	@Override public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
		return Components.dagger.of(stack) == null && super.canPerformAction(stack, toolAction);
	}
}
