package soulboundarmory.skill;

import net.minecraft.item.Items;
import soulboundarmory.module.gui.widget.Widget;

public class ClimbingSkill extends Skill {
	public ClimbingSkill() {
		super("climbing", 1);
	}

	@Override public int cost(int level) {
		return 4;
	}

	@Override public void render(Widget<?> widget, int level) {
		widget.renderGuiItem(Items.LADDER, widget.absoluteX(), widget.absoluteY());
	}
}
