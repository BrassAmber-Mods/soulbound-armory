package soulboundarmory.mixin.mixin.client;

import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import soulboundarmory.component.Components;

@Mixin(ItemRenderer.class)
abstract class ItemRendererMixin {
	@ModifyVariable(method = "getModel", at = @At(value = "HEAD"), ordinal = 0)
	ItemStack changeModelToSourceSoulboundItemForReverseTransformationAnimation(ItemStack stack, ItemStack stack1, World world, LivingEntity entity) {
		var m = Components.marker.of(stack);
		var marker = Components.entityData.optional(entity).flatMap(m1 -> m1.animatingItem).orElse(null);

		return m != null && marker != null && m.item == marker.item && !marker.stack.isItemEqualIgnoreDamage(stack) ? marker.stack : stack;
	}
}
