package soulboundarmory.mixin.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import soulboundarmory.component.Components;
import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.skill.Skill;
import soulboundarmory.skill.Skills;
import soulboundarmory.util.Util;

@Mixin(Entity.class)
abstract class EntityMixin {
	@ModifyVariable(method = "move", ordinal = 1, at = @At(value = "STORE", ordinal = 0))
	Vec3d recordClimbingClawsAndShoeSpikes(Vec3d adjustedMovement, MovementType movementType, Vec3d movement) {
		if (Util.<Entity>cast(this) instanceof PlayerEntity player) {
			var climbing = Components.armor.of(player).climbing = adjustedMovement.y >= 0 || (adjustedMovement.x == movement.x && adjustedMovement.z == movement.z) ? 0
				: has(player, ItemComponentType.chestplate, EquipmentSlot.CHEST, Skills.climbingClaws) + has(player, ItemComponentType.boots, EquipmentSlot.FEET, Skills.shoeSpikes);

			if (climbing > 0 && player.isHoldingOntoLadder()) {
				return adjustedMovement.withAxis(Direction.Axis.Y, Math.max(adjustedMovement.y, 0));
			}
		}

		return adjustedMovement;
	}

	@Unique private static int has(LivingEntity entity, ItemComponentType type, EquipmentSlot slot, Skill skill) {
		var item = type.of(entity);
		return item.matches(entity.getEquippedStack(slot)) && item.hasSkill(skill) ? 1 : 0;
	}
}
