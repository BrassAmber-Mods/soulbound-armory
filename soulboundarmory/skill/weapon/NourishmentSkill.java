package soulboundarmory.skill.weapon;

import net.minecraft.item.Items;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.skill.Skill;

public final class NourishmentSkill extends Skill {
	public NourishmentSkill() {
		super("nourishment", 3);
	}

	@Override public int cost(int level) {
		return level == 1 ? 3 : level;
	}

	@Override public void render(Widget<?> widget, int level) {
		widget.renderGuiItem(Items.COOKED_BEEF, widget.absoluteX(), widget.absoluteY());
	}
}
