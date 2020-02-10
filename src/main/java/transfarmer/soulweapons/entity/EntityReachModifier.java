package transfarmer.soulweapons.entity;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import transfarmer.soulweapons.capability.ISoulWeapon;

import static transfarmer.soulweapons.capability.SoulWeaponProvider.CAPABILITY;

public class EntityReachModifier extends EntityArrow {
    private float reachDistance;

    public EntityReachModifier(World worldIn) {
        super(worldIn);
    }

    public EntityReachModifier(final World world, final double x, final double y, final double z) {
        super(world, x, y, z);
    }

    public EntityReachModifier(final World world, final EntityLivingBase shooter, final float reachDistance) {
        this(world, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.1, shooter.posZ);
        this.world = world;
        this.shootingEntity = shooter;
        this.reachDistance = reachDistance + 1;
        this.setSize(0, 0);
    }

    @Override
    protected ItemStack getArrowStack() {
        return null;
    }

    public void onUpdate() {
        Vec3d pos = new Vec3d(this.posX, this.posY, this.posZ);
        Vec3d newPos = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        RayTraceResult rayTraceResult = this.world.rayTraceBlocks(pos, newPos, false, true, false);

        if (rayTraceResult != null) {
            newPos = new Vec3d(rayTraceResult.hitVec.x, rayTraceResult.hitVec.y, rayTraceResult.hitVec.z);
        }

        final Entity entity = this.findEntityOnPath(pos, newPos);

        if (entity != null) {
            rayTraceResult = new RayTraceResult(entity);
        }

        if (rayTraceResult != null && rayTraceResult.entityHit instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) rayTraceResult.entityHit;

            if (this.shootingEntity instanceof EntityPlayer && !((EntityPlayer) this.shootingEntity).canAttackPlayer(entityplayer)) {
                rayTraceResult = null;
            }
        }

        if (rayTraceResult != null && !ForgeEventFactory.onProjectileImpact(this, rayTraceResult)) {
            this.onHit(rayTraceResult);
        }

        this.setPosition(this.posX, this.posY, this.posZ);
        this.doBlockCollisions();
    }

    @Override
    protected void onHit(RayTraceResult result) {
        if (!this.world.isRemote && result.entityHit != this.shootingEntity && this.shootingEntity instanceof EntityPlayer) {
            final Entity target = result.entityHit;
            final EntityPlayer player = (EntityPlayer) this.shootingEntity;
            final ISoulWeapon capability = player.getCapability(CAPABILITY, null);

            if (target != null) {
                if (this.distanceToHit(result) <= this.reachDistance * this.reachDistance
                    && ForgeHooks.onPlayerAttackTarget(player, target) && target.canBeAttackedWithItem() && !target.hitByEntity(player)) {

                    float attackDamageModifier = (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                    float attackDamageRatio;

                    if (target instanceof EntityLivingBase) {
                        attackDamageRatio = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), ((EntityLivingBase) target).getCreatureAttribute());
                    } else {
                        attackDamageRatio = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), EnumCreatureAttribute.UNDEFINED);
                    }

                    final float cooldownRatio = capability.getAttackRatio(capability.getCurrentType());
                    attackDamageModifier *= 0.2 + cooldownRatio * cooldownRatio * 0.8;
                    attackDamageRatio *= cooldownRatio;

                    if (attackDamageModifier > 0 || attackDamageRatio > 0) {
                        boolean strong = cooldownRatio > 0.9F;
                        boolean knockback = player.isSprinting() && strong;
                        int knockbackModifier = EnchantmentHelper.getKnockbackModifier(player);

                        if (knockback) {
                            player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, player.getSoundCategory(), 1, 1);
                            knockbackModifier++;
                            knockback = true;
                        }

                        boolean critical = strong && player.fallDistance > 0 && !player.onGround && !player.isOnLadder()
                            && !player.isInWater() && !player.isPotionActive(MobEffects.BLINDNESS) && !player.isRiding()
                            && target instanceof EntityLivingBase && !player.isSprinting();

                        CriticalHitEvent hitResult = ForgeHooks.getCriticalHit(player, target, critical, critical ? 1.5F : 1);
                        critical = hitResult != null;

                        if (critical) {
                            attackDamageModifier *= hitResult.getDamageModifier();
                        }

                        attackDamageModifier += attackDamageRatio;
                        final double speed = player.distanceWalkedModified - player.prevDistanceWalkedModified;
                        final boolean sweep = strong && !critical && !knockback && player.onGround && speed < player.getAIMoveSpeed() && player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSword;
                        float initialHealth = 0;
                        boolean burn = false;
                        int fireAspectModifier = EnchantmentHelper.getFireAspectModifier(player);

                        if (target instanceof EntityLivingBase) {
                            initialHealth = ((EntityLivingBase) target).getHealth();

                            if (fireAspectModifier > 0 && !target.isBurning()) {
                                burn = true;
                                target.setFire(1);
                            }
                        }

                        final double motionX = target.motionX;
                        final double motionY = target.motionY;
                        final double motionZ = target.motionZ;

                        if (target.attackEntityFrom(DamageSource.causePlayerDamage(player), attackDamageModifier)) {
                            if (knockbackModifier > 0) {
                                if (target instanceof EntityLivingBase) {
                                    ((EntityLivingBase) target).knockBack(player, knockbackModifier * 0.5F, MathHelper.sin(player.rotationYaw * 0.017453292F), -MathHelper.cos(player.rotationYaw * 0.017453292F));
                                } else {
                                    target.addVelocity(-MathHelper.sin(player.rotationYaw * 0.017453292F) * knockbackModifier * 0.5F, 0.1D, MathHelper.cos(player.rotationYaw * 0.017453292F) * knockbackModifier * 0.5F);
                                }

                                player.motionX *= 0.6D;
                                player.motionZ *= 0.6D;
                                player.setSprinting(false);
                            }

                            if (sweep) {
                                float f3 = 1.0F + EnchantmentHelper.getSweepingDamageRatio(player) * attackDamageModifier;

                                for (EntityLivingBase entitylivingbase : player.world.getEntitiesWithinAABB(EntityLivingBase.class, target.getEntityBoundingBox().grow(1, 0.25, 1))) {
                                    if (entitylivingbase != player && entitylivingbase != target && !player.isOnSameTeam(entitylivingbase) && player.getDistanceSq(entitylivingbase) < 9) {
                                        entitylivingbase.knockBack(player, 0.4F, MathHelper.sin(player.rotationYaw * 0.017453292F), -MathHelper.cos(player.rotationYaw * 0.017453292F));
                                        entitylivingbase.attackEntityFrom(DamageSource.causePlayerDamage(player), f3);
                                    }
                                }

                                player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), 1, 1);
                                player.spawnSweepParticles();
                            }

                            if (target instanceof EntityPlayerMP && target.velocityChanged) {
                                ((EntityPlayerMP) target).connection.sendPacket(new SPacketEntityVelocity(target));
                                target.velocityChanged = false;
                                target.motionX = motionX;
                                target.motionY = motionY;
                                target.motionZ = motionZ;
                            }

                            if (critical) {
                                player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1, 1);
                                player.onCriticalHit(target);
                            }

                            if (!critical && !sweep) {
                                if (strong) {
                                    player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.getSoundCategory(), 1, 1);
                                } else {
                                    player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, player.getSoundCategory(), 1, 1);
                                }
                            }

                            if (attackDamageRatio > 0) {
                                player.onEnchantmentCritical(target);
                            }

                            player.setLastAttackedEntity(target);

                            if (target instanceof EntityLivingBase) {
                                EnchantmentHelper.applyThornEnchantments((EntityLivingBase) target, player);
                            }

                            EnchantmentHelper.applyArthropodEnchantments(player, target);

                            if (target instanceof EntityLivingBase) {
                                float damageDealt = initialHealth - ((EntityLivingBase) target).getHealth();
                                player.addStat(StatList.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));

                                if (fireAspectModifier > 0) {
                                    target.setFire(fireAspectModifier * 4);
                                }

                                if (player.world instanceof WorldServer && damageDealt > 2.0F) {
                                    int k = (int) ((double) damageDealt * 0.5D);
                                    ((WorldServer) player.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, target.posX, target.posY + (double) (target.height * 0.5F), target.posZ, k, 0.1D, 0.0D, 0.1D, 0.2D);
                                }
                            }

                            player.addExhaustion(0.1F);
                        } else {
                            player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory(), 1.0F, 1.0F);

                            if (burn) {
                                target.extinguish();
                            }
                        }
                    }
                }
            }

            capability.resetCooldown(capability.getCurrentType());
        }

        this.setDead();
    }

    public void shoot(final double x, final double y, final double z) {
        this.motionX = x * 255;
        this.motionY = y * 255;
        this.motionZ = z * 255;
    }

    private double distanceToHit(final RayTraceResult rayTraceResult) {
        final Vec3d pos = rayTraceResult.hitVec;

        return Math.pow(pos.x - this.posX, 2) + Math.pow(pos.y - this.posY, 2) + Math.pow(pos.z - this.posZ, 2);
    }
}
