package soulboundarmory.skill;

import net.minecraftforge.registries.IForgeRegistry;
import soulboundarmory.module.transform.Register;
import soulboundarmory.module.transform.RegisterAll;
import soulboundarmory.module.transform.Registry;
import soulboundarmory.skill.tool.common.AbsorptionSkill;
import soulboundarmory.skill.tool.common.CircumspectionSkill;
import soulboundarmory.skill.tool.common.EnderPullSkill;
import soulboundarmory.skill.weapon.*;

@RegisterAll(type = Skill.class, registry = "skill")
public class Skills {
	// public static final Skill ambidexterity = new AmbidexteritySkill(SoulboundArmory.id("ambidexterity"));
	@Register("absorption") public static final Skill absorption = new AbsorptionSkill();
	@Register("cushion") public static final Skill cushion = new CushionSkill();
	@Register("circumspection") public static final Skill circumspection = new CircumspectionSkill();
	@Register("climbing_claws") public static final Skill climbingClaws = new ClimbingSkill();
	@Register("ender_pull") public static final Skill enderPull = new EnderPullSkill();
	@Register("floccinaucinihilipilification") public static final Skill floccinaucinihilipilification = new FloccinaucinihilipilificationSkill();
	@Register("freezing") public static final Skill freezing = new FreezingSkill();
	@Register("leaping") public static final Skill leaping = new LeapingSkill();
	@Register("nourishment") public static final Skill nourishment = new NourishmentSkill();
	@Register("precision") public static final Skill precision = new PrecisionSkill();
	@Register("return") public static final Skill returne = new ReturnSkill();
	@Register("shadow_clone") public static final Skill shadowClone = new ShadowCloneSkill();
	@Register("shoe_spikes") public static final Skill shoeSpikes = new ClimbingSkill();
	@Register("sneak_return") public static final Skill sneakReturn = new SneakReturnSkill();

	@Registry("skill") public static native IForgeRegistry<Skill> registry();
}
