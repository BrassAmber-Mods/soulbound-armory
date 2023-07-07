package soulboundarmory.mixin.mixin.client;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import soulboundarmory.item.SoulboundItems;

@Mixin(HeldItemRenderer.class)
abstract class HeldItemRendererMixin {
	@ModifyConstant(method = "renderFirstPersonItem", constant = @Constant(doubleValue = 0.7F))
	double lowerDaggerTranslation(double pointSeven, AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		return item.isOf(SoulboundItems.dagger) ? 0.4 : pointSeven;
	}

	@ModifyConstant(method = "renderFirstPersonItem", constant = @Constant(doubleValue = 0.1F, ordinal = 0))
	double translateDaggerRight(double pointOne, AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		return item.isOf(SoulboundItems.dagger) ? 0.4 : pointOne;
	}

	@Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "net/minecraft/client/render/item/HeldItemRenderer.renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
	void blockWithSoulboundSword(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrixes, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {
		if (item.isOf(SoulboundItems.sword) && player.isUsingItem() && player.getActiveHand() == hand) {
			var mainArm = player.getMainArm();
			var direction = (hand == Hand.MAIN_HAND ? mainArm : mainArm.getOpposite()) == Arm.RIGHT ? 1 : -1;
			matrixes.translate(direction * -0.08, 0.1, direction * 0.03);
			matrixes.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(direction * 80));
			matrixes.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(direction * 90));
		}
	}
}
