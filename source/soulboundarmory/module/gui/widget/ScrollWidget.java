package soulboundarmory.module.gui.widget;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import soulboundarmory.util.Util;

import java.util.function.Predicate;

public class ScrollWidget extends Widget<ScrollWidget> {
	private final Widget<?> scrollbar = new ScalableWidget<>()
		.texture(Util.id("textures/gui/scrollbar.png"))
		.textureSize(16, 64)
		.uv(8, 0)
		.slice(0, 8, 8, 0, 8, 64)
		.width(8)
		.heightProportion(sb -> {
			var totalHeight = this.bodyHeight();
			var height = this.height();
			return totalHeight <= height ? height : (double) height / totalHeight;
		})
		.x(1D).alignRight()
		.y(sb -> (int) (this.ratio() * (this.height() - sb.height())))
		.present(() -> this.height() < this.bodyHeight());

	private int scroll;

	public ScrollWidget() {
		this.add(this.scrollbar);
	}

	public int bodyHeight() {
		return this.descendants().filter(Predicate.not(this.scrollbar::equals)).mapToInt(Widget::absoluteEndY).max().orElse(0)
			- this.descendants().filter(Predicate.not(this.scrollbar::equals)).mapToInt(Widget::absoluteY).min().orElse(0);
	}

	@Override public boolean focusable() {
		return this.isActive();
	}

	@Override public void render(MatrixStack matrixes) {
		pushScissor(matrixes, this.absoluteX(), this.absoluteY(), this.width(), this.height());
		matrixes.push();
		matrixes.translate(0, -this.scroll, 0);
		super.render(matrixes);
		matrixes.pop();
		popScissor(matrixes);
	}

	@Override protected boolean scroll(double amount) {
		var max = this.max();

		if (this.scroll >= max && amount < 0) {
			this.scroll = max;
			return false;
		}

		if (this.scroll <= 0 && amount > 0) {
			this.scroll = 0;
			return false;
		}

		super.scroll(amount);
		this.scroll = (int) MathHelper.clamp(this.scroll - 16 * amount, 0, max);

		return true;
	}

	private double ratio() {
		return (double) this.scroll / this.max();
	}

	private int max() {
		return this.bodyHeight() - this.height();
	}
}
