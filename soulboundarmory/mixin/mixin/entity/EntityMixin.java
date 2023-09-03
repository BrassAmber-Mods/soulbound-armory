package soulboundarmory.mixin.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import soulboundarmory.component.Components;
import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.network.ExtendedPacketBuffer;
import soulboundarmory.network.Packets;
import soulboundarmory.skill.Skill;
import soulboundarmory.skill.Skills;
import soulboundarmory.util.Util;

@Mixin(Entity.class)
abstract class EntityMixin {
	@ModifyVariable(method = "move", ordinal = 1, at = @At(value = "STORE", ordinal = 0))
	Vec3d recordClimbingClawsAndShoeSpikes(Vec3d adjustedMovement, MovementType movementType, Vec3d movement) {
		if (Util.<Entity>cast(this) instanceof PlayerEntity player) {
			var armor = Components.armor.of(player);

			if (armor.climbing > 0) {
				player.onLanding();
			}

			if (player.world.isClient) {
				armor.climbing = adjustedMovement.y >= 0 || (adjustedMovement.x == movement.x && adjustedMovement.z == movement.z) ? 0
					: has(player, ItemComponentType.chestplate, EquipmentSlot.CHEST, Skills.climbingClaws) + has(player, ItemComponentType.boots, EquipmentSlot.FEET, Skills.shoeSpikes);

				Packets.serverClimb.send(new ExtendedPacketBuffer().writeByte(armor.climbing));

				if (armor.climbing > 0 && player.isHoldingOntoLadder()) {
					return adjustedMovement.withAxis(Direction.Axis.Y, Math.max(adjustedMovement.y, 0));
				}
			}
		}

		return adjustedMovement;
	}

	@Inject(method = "move", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER, target = "Lnet/minecraft/entity/Entity;verticalCollision:Z"))
	void disableCollisionWithClimbingClawsAndShoeSpikes(MovementType movementType, Vec3d movement, CallbackInfo control) {
		if (Util.<Entity>cast(this) instanceof PlayerEntity player && Components.armor.of(player).climbing > 0 && player.isHoldingOntoLadder()) {
			player.verticalCollision = false;
		}
	}

	@Inject(method = "bypassesSteppingEffects", cancellable = true, at = @At(value = "HEAD"))
	void applyAwareness(CallbackInfoReturnable<Boolean> control) {
		if (Util.<Entity>cast(this) instanceof PlayerEntity player && has(player, ItemComponentType.boots, EquipmentSlot.FEET, Skills.cushion) == 1) {
			control.setReturnValue(true);
		}
	}

	@Unique private static int has(LivingEntity entity, ItemComponentType type, EquipmentSlot slot, Skill skill) {
		var item = type.of(entity);
		return item.matches(entity.getEquippedStack(slot)) && item.hasSkill(skill) ? 1 : 0;
	}
}
