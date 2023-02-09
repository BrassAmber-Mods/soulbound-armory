package soulboundarmory.client.gui.screen;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import soulboundarmory.client.i18n.Translations;
import soulboundarmory.module.gui.widget.*;
import soulboundarmory.skill.SkillInstance;

import java.util.List;

public class SkillWidget extends Widget<SkillWidget> {
	private final ScalableWidget<?> frame = this.add(new ScalableWidget<>().width(24).height(24));
	private final SkillTab tab;
	private final SkillInstance skill;

	public SkillWidget(SkillTab tab, SkillInstance skill) {
		this.tab = tab;
		this.skill = skill;
		this.frame.add(new GraphicWidget(widget -> skill.render(widget, this.matrixes)).center().x(0.5).y(0.5).size(16));
		this.primaryAction(() -> tab.container().item().upgrade(skill));
	}

	@Override public void tick() {
		if (this.isFocused()) {
			var cost = this.skill.cost();
			var genericSections = new ReferenceArrayList<Text>();

			if (this.skill.learned()) {
				if (this.skill.skill.isTiered()) {
					genericSections.add(this.skill.skill.maxLevel < 0 ? Translations.guiLevel.text(this.skill.level()) : Translations.guiLevelFinite.text(this.skill.level(), this.skill.skill.maxLevel));

					if (this.skill.canUpgrade()) {
						genericSections.add(Translations.guiSkillUpgradeCost.text(cost));
					}
				}
			} else if (this.skill.dependenciesFulfilled()) {
				genericSections.add(Translations.guiSkillLearnCost.text(cost));
			}

			var sections = ReferenceArrayList.<List<? extends StringVisitable>>of();
			var tooltipWidth = Math.max(36 + Math.max(108, textRenderer.getWidth(this.skill.name())), 12 + genericSections.stream().peek(section -> sections.add(List.of(section))).mapToInt(textRenderer::getWidth).max().orElse(0));
			var tooltip = wrap(this.skill.tooltip(), tooltipWidth - 8);
			sections.add(0, tooltip);

			var height = 1 + (1 + tooltip.size()) * fontHeight();
			var totalHeight = height + 20 * sections.size();
			var y = this.absoluteY() + totalHeight > this.tab.endY() ? -totalHeight : -7;
			var textY = 7;
			this.clear();

			var sectionBox = new WidgetBox<>().vertical().x(-4).y(1, y);
			this.tooltip(sectionBox);

			for (var section : sections) {
				sectionBox.add(new ScalableWidget<>())
					.grayRectangle()
					.width(tooltipWidth)
					.height(height)
					.present(this::isFocused).with(new TextWidget().alignStart().x(5).y(textY).color(0x999999).with(t -> section.forEach(t::text)));

				textY = 6;
				height = 20;
			}

			this.tooltip(new ScalableWidget<>()
				.blueRectangle()
				.x(-4)
				.y(0.5)
				.centerY()
				.width(tooltipWidth)
				.height(20)
				.present(this::isFocused)
				.with(new TextWidget().x(32).y(6).shadow().text(this.skill.name()))
			);

			this.add(this.frame);
		}

		super.tick();
	}

	@Override protected void render() {
		if (this.skill.learned()) {
			this.frame.yellowRectangle();
		} else {
			this.frame.whiteRectangle();
		}

		if (this.isFocused()) {
			this.frame.color3f(1);
			this.deferRender();
		} else {
			this.frame.color3f(this.tab.brightness);
		}
	}
}
