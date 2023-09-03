package soulboundarmory.skill.weapon;

import net.minecraft.item.Items;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.skill.Skill;

public class ThrowingSkill extends Skill {
	public ThrowingSkill() {
		super("throwing", 1);
	}

	@Override public int cost(int level) {
		return 2;
	}

	@Override public void render(Widget<?> widget, int level) {
		widget.renderGuiItem(Items.TRIDENT, widget.absoluteX(), widget.absoluteY());
	}
}
