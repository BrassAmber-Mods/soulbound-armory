package soulboundarmory.item;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.skill.Skills;

public class SoulboundGreatswordItem extends SoulboundMeleeWeapon {
	public SoulboundGreatswordItem() {
		super(5, -3.2F, 3);
	}

	@Override public int getMaxUseTime(ItemStack stack) {
		return 200;
	}

	@Override public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}

	@Override public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if (ItemComponentType.greatsword.of(player).hasSkill(Skills.leaping)) {
			player.setCurrentHand(hand);

			return TypedActionResult.consume(player.getStackInHand(hand));
		}

		return TypedActionResult.fail(player.getStackInHand(hand));
	}

	@Override public void onStoppedUsing(ItemStack itemStack, World world, LivingEntity user, int timeLeft) {
		ItemComponentType.greatsword.nullable(user).ifPresent(component -> {
			var timeTaken = 200 - timeLeft;

			if (timeTaken > 5) {
				var look = user.getRotationVector();
				var maxSpeed = 1.25F;
				var speed = Math.min(maxSpeed, timeTaken / 20F * maxSpeed);

				user.addVelocity(look.x * speed, look.y * speed / 2 + 0.2, look.z * speed);
				user.setSprinting(true);
				component.leap(speed / maxSpeed);
			}
		});
	}

	@OnlyIn(Dist.CLIENT)
	@Override public void inventoryTick(ItemStack itemStack, World world, Entity entity, int itemSlot, boolean isSelected) {
		if (entity instanceof ClientPlayerEntity user && isSelected && user.getActiveItem().getItem() == this) {
			user.input.movementForward *= 4.5;
			user.input.movementSideways *= 4.5;
		}
	}
}
