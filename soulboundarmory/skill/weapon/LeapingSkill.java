package soulboundarmory.skill.weapon;

import net.minecraft.item.Items;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.skill.Skill;

public class LeapingSkill extends Skill {
	public LeapingSkill() {
		super("leaping", 1);
	}

	@Override public int cost(int level) {
		return 3;
	}

	@Override public void render(Widget<?> widget, int level) {
		widget.renderGuiItem(Items.RABBIT_FOOT, widget.absoluteX(), widget.absoluteY());
	}
}
