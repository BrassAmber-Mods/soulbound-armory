package net.auoeke.soulboundarmory.skill.weapon.dagger;

import net.auoeke.soulboundarmory.registry.Skills;
import net.auoeke.soulboundarmory.skill.Skill;
import net.minecraft.util.ResourceLocation;

public class ReturnSkill extends Skill {
    public ReturnSkill(ResourceLocation identifier) {
        super(identifier);
    }

    @Override
    public void initDependencies() {
        this.dependencies.add(Skills.throwing);

        super.initDependencies();
    }

    @Override
    public int cost(boolean learned, int level) {
        return 2;
    }
}
