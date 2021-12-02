package soulboundarmory.item;

import soulboundarmory.component.soulbound.item.tool.PickStorage;
import soulboundarmory.component.soulbound.item.weapon.SwordStorage;
import soulboundarmory.entity.SoulboundLightningEntity;
import soulboundarmory.registry.Skills;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class SoulboundSword extends SoulboundMeleeWeapon {
    public SoulboundSword() {
        super(3, -2.4F, 0);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BLOCK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        SwordStorage component = (SwordStorage) PickStorage.get(player, this).get();

        if (!world.isClient && component.hasSkill(Skills.summonLightning) && component.getLightningCooldown() <= 0) {
            Vec3d pos = player.getPos();
            BlockHitResult result = world.raycast(new RaycastContext(pos, pos.add(player.getRotationVector()).multiply(512, 512, 512), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));

            if (result != null) {
                player.world.spawnEntity(new SoulboundLightningEntity(player.world, result.getPos(), player.getUuid()));
                component.resetLightningCooldown();

                return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
            }
        }

        return new TypedActionResult<>(ActionResult.FAIL, player.getStackInHand(hand));
    }
}