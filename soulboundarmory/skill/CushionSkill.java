package soulboundarmory.skill;

import net.minecraft.item.Items;
import soulboundarmory.module.gui.widget.Widget;

public class CushionSkill extends Skill {
	public CushionSkill() {
		super("cushion", 1);
	}

	@Override public int cost(int level) {
		return 4;
	}

	@Override public void render(Widget<?> widget, int level) {
		widget.renderGuiItem(Items.WHITE_WOOL, widget.absoluteX(), widget.absoluteY());
	}
}
