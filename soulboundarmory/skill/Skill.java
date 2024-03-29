package soulboundarmory.skill;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.RegisterEvent;
import soulboundarmory.SoulboundArmory;
import soulboundarmory.text.Translations;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.registry.Identifiable;
import soulboundarmory.util.Util;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 An active or passive ability; locked by default and possibly having multiple levels and dependencies.
 */
@EventBusSubscriber(modid = SoulboundArmory.ID, bus = EventBusSubscriber.Bus.MOD)
public abstract class Skill extends Identifiable {
	public final int maxLevel;

	protected final Identifier texture;

	private int tier = -1;

	public Skill(Identifier identifier, int maxLevel) {
		this.texture = new Identifier(identifier.getNamespace(), "textures/skill/%s.png".formatted(identifier.getPath()));
		this.maxLevel = maxLevel;
	}

	public Skill(String path, int maxLevel) {
		this(Util.id(path), maxLevel);
	}

	public Skill(String path) {
		this(path, -1);
	}

	/**
	 @param level the next level; 1 is the level reached after unlocking; never <= 0.
	 @return the cost of unlocking or upgrading this skill; never < 0
	 */
	public abstract int cost(int level);

	/**
	 @return whether this skill has multiple levels
	 */
	public final boolean isTiered() {
		return this.maxLevel != 1;
	}

	/**
	 A skill's tier is the number of levels of dependencies that it has.

	 @return this skill's tier
	 */
	public final int tier() {
		return this.tier;
	}

	public Set<Skill> dependencies() {
		return Collections.emptySet();
	}

	public boolean hasDependencies() {
		return !this.dependencies().isEmpty();
	}

	public Text name() {
		return Translations.skillName(this);
	}

	public List<? extends StringVisitable> tooltip() {
		return Translations.skillDescription(this);
	}

	/**
	 Render an icon of this skill.
	 */
	@OnlyIn(Dist.CLIENT)
	public void render(Widget<?> widget, int level) {
		Widget.shaderTexture(this.texture);
		DrawableHelper.drawTexture(widget.matrixes, widget.absoluteX(), widget.absoluteY(), widget.z(), 0, 0, 16, 16, 16, 16);
	}

	/**
	 Register dependencies for this skill.
	 Must be invoked <b>after</b> all skills are initialized.
	 */
	private void initializeDependencies() {
		if (this.tier == -1) {
			this.tier = this.hasDependencies() ? 1 : 0;

			for (var dependency : this.dependencies()) {
				dependency.initializeDependencies();
				this.tier = Math.max(this.tier, dependency.tier() + 1);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void initializeDependencies(RegisterEvent event) {
		event.register(Skills.registry().getRegistryKey(), helper -> Skills.registry().forEach(Skill::initializeDependencies));
	}
}
