package soulboundarmory.skill.weapon;

import java.util.Collections;
import java.util.Set;
import net.minecraft.item.Items;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.skill.Skill;
import soulboundarmory.skill.Skills;

public class FreezingSkill extends Skill {
	public FreezingSkill() {
		super("freezing", 1);
	}

	@Override public Set<Skill> dependencies() {
		return Collections.singleton(Skills.leaping);
	}

	@Override public int cost(int level) {
		return 2;
	}

	@Override public void render(Widget<?> widget, int level) {
		widget.renderGuiItem(Items.SNOWBALL, widget.absoluteX(), widget.absoluteY());
	}
}
