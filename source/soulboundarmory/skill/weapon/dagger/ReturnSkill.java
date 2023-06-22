package soulboundarmory.skill.weapon.dagger;

import soulboundarmory.skill.Skill;

public class ReturnSkill extends Skill {
	public ReturnSkill() {
		super("return", 1);
	}

	@Override public int cost(int level) {
		return 2;
	}
}
