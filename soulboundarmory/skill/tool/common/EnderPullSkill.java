package soulboundarmory.skill.tool.common;

import net.minecraft.item.Items;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.skill.Skill;

public final class EnderPullSkill extends Skill {
	public EnderPullSkill() {
		super("ender_pull", 1);
	}

	@Override public int cost(int level) {
		return 3;
	}

	@Override public void render(Widget<?> widget, int level) {
		widget.renderGuiItem(Items.ENDER_PEARL, widget.absoluteX(), widget.absoluteY());
	}
}
