package soulboundarmory.skill.weapon;

import net.minecraft.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.skill.Skill;

public class FloccinaucinihilipilificationSkill extends Skill {
	public FloccinaucinihilipilificationSkill() {
		super("floccinaucinihilipilification", 1);
	}

	@Override public int cost(int level) {
		return 10;
	}

	@OnlyIn(Dist.CLIENT)
	@Override public void render(Widget<?> widget, int level) {
		widget.renderGuiItem(Items.SHIELD, widget.absoluteX(), widget.absoluteY());
	}
}
