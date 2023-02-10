package soulboundarmory.module.gui.widget;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import soulboundarmory.util.Math2;
import soulboundarmory.util.Util;

import java.util.function.Predicate;

public class ScrollWidget extends Widget<ScrollWidget> {
	private final Widget<?> scrollbar = new ScalableWidget<>()
		.texture(Util.id("textures/gui/scrollbar.png"))
		.textureSize(8, 64)
		.slice(0, 8, 8, 0, 8, 64)
		.width(8)
		.height(sb -> {
			var totalHeight = this.bodyHeight();
			var height = this.height();
			return totalHeight <= height ? height : Math2.square(height) / totalHeight;
		})
		.x(1).alignEnd()
		.y(sb -> this.scroll / this.max() * (this.height() - sb.height()))
		.present(() -> this.height() < this.bodyHeight());

	private int scroll;

	public ScrollWidget() {
		this.add(this.scrollbar);
	}

	public int bodyHeight() {
		return this.descendants().filter(Predicate.not(this.scrollbar::equals)).mapToInt(Widget::absoluteEndY).max().orElse(0)
			- this.descendants().filter(Predicate.not(this.scrollbar::equals)).mapToInt(Widget::absoluteY).min().orElse(0);
	}

	@Override public void render(MatrixStack matrixes) {
		pushScissor(this.absoluteX(), this.absoluteY(), this.width(), this.height());
		matrixes.push();
		matrixes.translate(0, -this.scroll, 0);
		super.render(matrixes);
		matrixes.pop();
		popScissor();
	}

	@Override public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (super.mouseScrolled(mouseX, mouseY, amount)) {
			return true;
		}

		var max = this.max();

		if (this.scroll >= max && amount < 0) {
			this.scroll = max;
			return false;
		}

		if (this.scroll <= 0 && amount > 0) {
			this.scroll = 0;
			return false;
		}

		this.scroll = (int) MathHelper.clamp(this.scroll - 32 * amount, 0, max);

		return true;
	}

	private int max() {
		return this.bodyHeight() - this.height();
	}
}
