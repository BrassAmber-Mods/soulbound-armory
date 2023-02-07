package soulboundarmory.mixin.mixin.client;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.config.Configuration;
import soulboundarmory.module.gui.Node;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
	@Inject(method = "hasGlint", at = @At("HEAD"), cancellable = true)
	private void disableGlint(CallbackInfoReturnable<Boolean> info) {
		if (ItemComponent.of(Node.player(), (ItemStack) (Object) this).isPresent() && !Configuration.Client.enchantmentGlint) {
			info.setReturnValue(false);
		}
	}
}
