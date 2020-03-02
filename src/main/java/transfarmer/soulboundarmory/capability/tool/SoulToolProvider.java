package transfarmer.soulboundarmory.capability.tool;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import transfarmer.soulboundarmory.capability.ISoulCapability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("ConstantConditions")
public class SoulToolProvider implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(ISoulCapability.class)
    private static final Capability<ISoulCapability> CAPABILITY = null;
    private final ISoulCapability instance = CAPABILITY.getDefaultInstance();

    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CAPABILITY ? CAPABILITY.cast(this.instance) : null;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CAPABILITY;
    }

    @Override
    public NBTBase serializeNBT() {
        return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        CAPABILITY.getStorage().readNBT(CAPABILITY, instance, null, nbt);
    }

    public static ISoulCapability get(final Entity entity) {
        return entity.getCapability(CAPABILITY, null);
    }
}