package soulboundarmory.mixin.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.component.soulbound.item.tool.ToolComponent;
import soulboundarmory.skill.Skills;
import soulboundarmory.util.Util2;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
	@Inject(method = "postMine", at = @At("RETURN"))
	private void addXPByEnderPull(World world, BlockState state, BlockPos pos, PlayerEntity miner, CallbackInfo info) {
		ItemComponent.of(miner, Util2.cast(this)).ifPresent(component -> component.mined(state, pos));
	}

	@Inject(method = "use", at = @At("RETURN"))
	private void absorbTool(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info) {
		if (hand == Hand.MAIN_HAND && info.getReturnValue().getResult() == ActionResult.PASS) {
			ItemComponent.of(user, Util2.cast(this)).ifPresent(component -> {
				if (component instanceof ToolComponent tool && tool.hasSkill(Skills.absorption)) {
					tool.absorb();
				}
			});
		}
	}
}
