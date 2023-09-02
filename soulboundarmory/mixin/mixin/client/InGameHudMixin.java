package soulboundarmory.mixin.mixin.client;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import soulboundarmory.event.ClientEvents;

@Mixin(InGameHud.class)
abstract class InGameHudMixin {
	@Inject(method = "renderHotbarItem", at = @At("HEAD"), cancellable = true)
	void hideSoulboundItemWhileTransforming(int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed, CallbackInfo info) {
		if (ClientEvents.summoning(stack)) {
			info.cancel();
		}
	}
}
