package soulboundarmory.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import net.minecraft.item.Items;
import soulboundarmory.text.Translations;
import soulboundarmory.module.gui.widget.ItemWidget;
import soulboundarmory.module.gui.widget.TextWidget;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.skill.SkillInstance;

import java.util.Map;
import java.util.stream.IntStream;

/**
 The skill tab; design (not code of course) blatantly copied from the advancement screen.
 */
public class SkillTab extends Tab {
	protected final Map<SkillInstance, SkillWidget> skills = new Reference2ReferenceLinkedOpenHashMap<>();
	protected final Widget<?> container = new Widget<>().movable();

	protected float brightness = 1;

	public SkillTab() {
		super(Translations.guiSkills);
	}

	@Override public Widget<?> icon() {
		return new ItemWidget().item(Items.PAPER);
	}

	@Override public void initialize() {
		if (!this.dim()) {
			this.brightness = 1;
		}

		this.updateWidgets();

		var points = this.add(new TextWidget())
			.x.center()
			.y(this.button.absoluteEndY())
			.stroke()
			.text(() -> this.pointText(this.container().item().skillPoints()))
			.color(0xEEEEEE);

		this.add(this.container)
			.width(this.container.descendantWidth())
			.height(this.container.descendantHeight())
			.x.middle().x(0.5)
			.max.x.value(this.container().options.absoluteX() - 4 - this.container.width())
			.y.middle().y(Math.max(points.endY() + 8, this.container().height() / 2));
	}

	@Override protected void render() {
		var delta = 20 * tickDelta() / 255F;
		this.brightness = this.dim() ? Math.max(this.brightness - delta, 175 / 255F) : Math.min(this.brightness + delta, 1);
		brightness(this.brightness);
		RenderSystem.enableBlend();
	}

	private boolean dim() {
		return this.container.children().anyMatch(child -> child.tooltips.stream().anyMatch(Widget::isPresent));
	}

	private void updateWidgets() {
		var skills = this.container().item().skills.values();
		// 0: index of the current skill per tier (used in a loop)
		// 1: number of skills per tier
		var tierOrders = skills.stream().map(SkillInstance::tier).distinct().map(t -> new int[]{0, 0}).toArray(int[][]::new);
		for (var skill : skills) tierOrders[skill.tier()][1]++;

		var width = 2 + IntStream.range(1, tierOrders.length).map(tier -> tierOrders[tier][1] - 1).sum();

		for (var skill : skills) {
			var tier = skill.tier();
			var data = tierOrders[tier];
			var spacing = skill.hasDependencies() ? 48 : width * 24;
			var x = data[0] * spacing;

			if (skill.hasDependencies()) {
				var dependencies = skill.dependencies;
				var offset = (1 - data[1]) * spacing / 2;
				x += offset + dependencies.stream().mapToInt(d -> this.skills.get(d).x()).sum() / dependencies.size();
			}

			this.skills.computeIfAbsent(skill, s -> this.container.add(new SkillWidget(this, s).size(24))).x(x).y(32 * tier);
			data[0]++;
		}
	}
}
