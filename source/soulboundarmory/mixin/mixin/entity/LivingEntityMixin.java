package soulboundarmory.mixin.mixin.entity;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import soulboundarmory.component.Components;
import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.registry.Skills;
import soulboundarmory.util.EntityUtil;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> p_i48580_1_, World p_i48580_2_) {
        super(p_i48580_1_, p_i48580_2_);
    }

    @Inject(method = "createLivingAttributes", at = @At("RETURN"), cancellable = true)
    private static void createSoulboundArmoryAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        // info.getReturnValue().add(SAAttributes.criticalStrikeRate, 0).add(SAAttributes.efficiency, 1);
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "tickCramming",
            at = @At(value = "INVOKE_ASSIGN",
                     target = "Lnet/minecraft/world/World;getOtherEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    protected void freeze(CallbackInfo info, List<Entity> entities) {
        if ((Object) this instanceof PlayerEntity player && !this.world.isClient) {
            var greatsword = Components.weapon.of(player).item(ItemComponentType.greatsword);
            var leapForce = greatsword.leapForce();

            if (leapForce > 0) {
                if (greatsword.hasSkill(Skills.freezing)) {
                    for (var nearbyEntity : entities) {
                        greatsword.freeze(nearbyEntity, (int) (20 * leapForce), (float) EntityUtil.speed(player) * (float) leapForce);
                    }
                }

                if (greatsword.leapDuration() <= 0 && player.isOnGround() && (player.getVelocity().y <= 0.01 || player.isCreative())) {
                    greatsword.leapDuration(7);
                }

                if (player.isInLava()) {
                    greatsword.resetLeapForce();
                }
            }
        }
    }
}