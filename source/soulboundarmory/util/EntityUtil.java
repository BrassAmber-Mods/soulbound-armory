package soulboundarmory.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;

import java.util.UUID;

public class EntityUtil {
	public static double speed(Entity entity) {
		var velocity = entity.getVelocity();
		return Math.sqrt(velocity.x * velocity.x + velocity.y * velocity.y + velocity.z * velocity.z);
	}

	public static double attribute(LivingEntity entity, EntityAttribute attribute) {
		var instance = entity.getAttributeInstance(attribute);
		return instance == null ? 0 : instance.getValue();
	}

	public static Entity entity(UUID id) {
		for (var world : Util.server().getWorlds()) {
			var entity = world.getEntity(id);

			if (entity != null) {
				return entity;
			}
		}

		return null;
	}
}
