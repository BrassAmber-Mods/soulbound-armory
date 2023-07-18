package soulboundarmory.mixin.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import soulboundarmory.component.Components;
import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.skill.Skills;
import soulboundarmory.util.Util;

import java.util.List;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
	@Shadow int jumpingCooldown;
	@Shadow boolean jumping;

	@Shadow abstract void dropXp();
	@Shadow abstract int getXpToDrop();

	@Inject(method = "tickCramming",
	        at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/World;getOtherEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"),
	        locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	void freeze(CallbackInfo info, List<Entity> entities) {
		if ((Object) this instanceof ServerPlayerEntity player) {
			var greatsword = ItemComponentType.greatsword.of(player);
			var leapForce = greatsword.leapForce();

			if (leapForce > 0) {
				if (greatsword.hasSkill(Skills.freezing)) {
					entities.stream().filter(greatsword::canFreeze).forEach(entity -> greatsword.freeze(entity, (int) (20 * leapForce), (float) Util.speed(player) * leapForce));
				}

				if (greatsword.leapDuration <= 0 && player.isOnGround() && (player.getVelocity().y <= 0.01 || player.isCreative())) {
					greatsword.leapDuration = 7;
				}

				if (player.isInLava()) {
					greatsword.resetLeapForce();
				}
			}
		}
	}

	@Redirect(method = "drop", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;dropXp()V"))
	void givePlayerXPFromEnderPull(LivingEntity entity, DamageSource source) {
		var mixin = (LivingEntityMixin) (Object) entity;
		ItemComponent.fromAttacker(entity, source)
			.filter(component -> component.hasSkill(Skills.enderPull))
			.ifPresentOrElse(component -> component.player.addExperience(mixin.getXpToDrop()), mixin::dropXp);
	}

	@Inject(method = "isClimbing", cancellable = true, at = @At(value = "HEAD"))
	void applyClimbingClawsOrShoeSpikes(CallbackInfoReturnable<Boolean> info) {
		Components.armor.optional(Util.cast(this)).ifPresent(armor -> {
			if (armor.climbing > 0) {
				info.setReturnValue(true);
			}
		});
	}

	@ModifyConstant(method = "applyMovementInput", constant = @Constant(doubleValue = 0.2))
	double slideWithClimbingClawsOrShoeSpikes(double point2) {
		return Components.armor.optional(Util.cast(this)).filter(armor -> armor.climbing > 0).map(armor -> Math.min(0, armor.player.getVelocity().y)).orElse(point2);
	}

	@Inject(method = "tickMovement", at = @At(value = "INVOKE", ordinal = 2, target = "Lnet/minecraft/util/profiler/Profiler;pop()V"))
	void jumpWithClimbingClawsOrShoeSpikes(CallbackInfo info) {
		if (this.jumping && this.jumpingCooldown == 0) {
			Components.armor.optional(Util.cast(this)).ifPresent(armor -> {
				if (armor.climbing > 0) {
					armor.player.jump();
					armor.player.setVelocity(armor.player.getVelocity().add(armor.player.getRotationVector().multiply(-0.1, 0, -0.1)));
					this.jumpingCooldown = 10;
				}
			});
		}
	}

	@Inject(method = "isHoldingOntoLadder", cancellable = true, at = @At(value = "HEAD"))
	void clingToBlockWithClimbingClawsAndShoeSpikes(CallbackInfoReturnable<Boolean> info) {
		Components.armor.optional(Util.cast(this)).ifPresent(armor -> {
			if (armor.climbing > 0) {
				info.setReturnValue(armor.climbing == 2 && !armor.player.isSneaking());
			}
		});
	}
}
