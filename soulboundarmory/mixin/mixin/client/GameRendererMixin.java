package soulboundarmory.mixin.mixin.client;

import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.skill.Skills;
import soulboundarmory.util.Math2;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
	@ModifyVariable(method = "updateTargetedEntity", at = @At(value = "STORE", ordinal = 3), ordinal = 1)
	private double reachThroughNonCollidableBlocks(double blockDistance, float tickDelta) {
		return Widget.client.cameraEntity instanceof PlayerEntity player ? ItemComponent.of(player, player.getMainHandStack())
			.filter(component -> component.hasSkill(Skills.precision))
			.map(component -> {
				var start = player.getCameraPosVec(tickDelta);
				var reach = Widget.client.interactionManager.getReachDistance();
				var result = player.world.raycast(new RaycastContext(start, start.add(player.getRotationVec(tickDelta).multiply(reach)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));

				return result == null ? Math2.square(reach) : result.squaredDistanceTo(player);
			}).orElse(blockDistance) : blockDistance;
	}

	@Inject(method = "renderFloatingItem", cancellable = true, at = @At("HEAD"))
	void hideTransformingSoulboundItemFromNonFirstPersonViews(int scaledWidth, int scaledHeight, float tickDelta, CallbackInfo control) {
		if (Widget.client.options.getPerspective() != Perspective.FIRST_PERSON) {
			control.cancel();
		}
	}

	@Redirect(method = "renderFloatingItem", at = @At(value = "INVOKE", ordinal = 2, target = "Lnet/minecraft/util/math/MathHelper;sin(F)F"))
	float reverseTransformingSoulboundItemDirection(float x) {
		return MathHelper.abs(MathHelper.cos(x * 0.75F));
	}
}
