package soulboundarmory.skill.weapon.dagger;

import net.minecraft.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.skill.Skill;
import soulboundarmory.skill.Skills;

import java.util.Collections;
import java.util.Set;

public class SneakReturnSkill extends Skill {
	public SneakReturnSkill() {
		super("sneak_return", 1);
	}

	@Override public Set<Skill> dependencies() {
		return Collections.singleton(Skills.returne);
	}

	@Override public int cost(int level) {
		return 1;
	}

	@OnlyIn(Dist.CLIENT)
	@Override public void render(Widget<?> widget, int level) {
		widget.renderGuiItem(Items.LEAD, widget.absoluteX(), widget.absoluteY());
	}
}
