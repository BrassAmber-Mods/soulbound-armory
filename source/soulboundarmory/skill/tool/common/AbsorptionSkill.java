package soulboundarmory.skill.tool.common;

import net.minecraft.item.Items;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.skill.Skill;

public final class AbsorptionSkill extends Skill {
	public AbsorptionSkill() {
		super("absorption", 1);
	}

	@Override public int cost(int level) {
		return 1;
	}

	@Override public void render(Widget<?> widget, int level) {
		widget.renderGuiItem(Items.SLIME_BALL, widget.absoluteX(), widget.absoluteY());
	}
}
