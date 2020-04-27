package transfarmer.soulboundarmory.skill.impl;

import transfarmer.soulboundarmory.skill.Skill;
import transfarmer.soulboundarmory.skill.SkillBase;
import transfarmer.soulboundarmory.util.CollectionUtil;

import java.util.List;

import static transfarmer.soulboundarmory.skill.Skills.THROWING;

public class SkillShadowClone extends SkillBase {
    public SkillShadowClone() {
        super("shadow_clone");
    }

    @Override
    public List<Skill> getDependencies() {
        return this.storage == null
                ? CollectionUtil.arrayList(new SkillThrowing())
                : CollectionUtil.arrayList(this.storage.get(this.item, THROWING));
    }

    @Override
    public int getCost() {
        return 2;
    }
}