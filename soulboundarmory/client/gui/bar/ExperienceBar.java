package soulboundarmory.client.gui.bar;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import soulboundarmory.text.Translations;
import soulboundarmory.client.texture.ExperienceBarTexture;
import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.config.Configuration;
import soulboundarmory.module.gui.widget.ScalableWidget;
import soulboundarmory.module.gui.widget.TextWidget;

public class ExperienceBar extends ScalableWidget<ExperienceBar> {
	private static final ExperienceBar overlayBar = new ExperienceBar().center().x(.5).y(1D, -27);

	protected final TextWidget level = this.add(new TextWidget())
		.x.center()
		.y(5).y.end()
		.color(Configuration.Client.Bar::argb)
		.stroke()
		.text(() -> this.component.level())
		.visible(w -> this.component.level() > 0)
		.tooltip(tooltip -> tooltip.y.end().text(() -> Translations.barLevel.text(this.component.level(), this.component.maxLevel() < 0 ? "∞" : this.component.maxLevel())));

	protected ItemComponent<?> component;

	public ExperienceBar() {
		this.experienceBar().texture(ExperienceBarTexture.instance).width(182).height(5);
	}

	public static boolean renderOverlay(MatrixStack matrixes) {
		overlayBar.item(ItemComponent.fromHands(client.player).orElse(null)).render(matrixes);
		return overlayBar.isPresent();
	}

	public ExperienceBar item(ItemComponent<?> component) {
		this.component = component;
		return this;
	}

	@Override public int getWidth(TextRenderer textRenderer) {
		return this.isPresent() ? this.width() + 8 : 0;
	}

	@Override public int getHeight() {
		return this.isPresent() ? this.height() + this.offset() + 6 : 0;
	}

	@Override public boolean isPresent() {
		return this.component != null && Configuration.Client.Bar.alpha > 25 && super.isPresent();
	}

	@Override public void render() {
		this.v(Configuration.Client.Bar.style.v).viewWidth(1D).color4f(Configuration.Client.Bar.getf(0), Configuration.Client.Bar.getf(1), Configuration.Client.Bar.getf(2), Configuration.Client.Bar.getf(3));
		super.render();

		if (this.component.canLevelUp()) {
			this.viewWidth(Math.min(1, this.component.experience() / this.component.nextLevelXP()));
		}

		this.v(Configuration.Client.Bar.style.v + 5);
		super.render();
	}

	@Override public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrixes, ItemRenderer itemRenderer, int z) {
		this.x(x).y(y + this.offset()).z(z).render(matrixes);
	}

	private int offset() {
		return this.component.level() > 0 ? fontHeight() - 2 : 0;
	}
}
