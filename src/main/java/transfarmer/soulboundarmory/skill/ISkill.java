package transfarmer.soulboundarmory.skill;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import transfarmer.soulboundarmory.statistics.Skills;
import transfarmer.soulboundarmory.statistics.base.iface.IItem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

public interface ISkill extends INBTSerializable<NBTTagCompound> {
    Set<ISkill> SKILLS = new HashSet<>();

    static ISkill get(final String name) {
        for (final ISkill skill : SKILLS) {
            if (skill.getRegistryName().equals(name)) {
                return skill;
            }
        }

        return null;
    }

    List<ISkill> getDependencies();

    boolean hasDependencies();

    int getTier();

    int getCost();

    boolean isLearned();

    boolean canBeLearned();

    boolean canBeLearned(int points);

    void learn();

    ResourceLocation getTexture();

    @NotNull
    String getModID();

    String getRegistryName();

    void setStorage(Skills storage, IItem item);

    @SideOnly(CLIENT)
    String getName();

    @SideOnly(CLIENT)
    List<String> getTooltip();
}
