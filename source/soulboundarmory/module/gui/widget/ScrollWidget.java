package soulboundarmory.module.gui.widget;

import net.minecraft.util.math.MathHelper;
import soulboundarmory.module.gui.box.Side;
import soulboundarmory.util.Util;

public class ScrollWidget extends Widget<ScrollWidget> {
	final Widget<?> scrollbar = new ScalableWidget<>()
		.texture(Util.id("textures/gui/scrollbar.png"))
		.textureSize(16, 64)
		.uv(8, 0)
		.slice(0, 8, 8, 0, 8, 64)
		.width(8)
		.heightProportion(sb -> (double) this.height() / this.bodyHeight())
		.x.end()
		.x(1D)
		.y.type(Side.Type.RELATIVE_FIXED)
		.y(sb -> (int) (this.ratio() * (this.height() - sb.height())))
		.present(() -> this.max() > 0);

	int scroll;

	public ScrollWidget() {
		this.add(this.scrollbar);
		this.height.base(this::bodyHeight);
	}

	public int bodyHeight() {
		var body = this.descendants(w -> w != this.scrollbar && w.isPresent() && !w.isTooltip()).toList();
		return body.isEmpty() ? 0 : body.stream().mapToInt(Widget::absoluteEndY).max().getAsInt() - body.stream().mapToInt(Widget::absoluteY).min().getAsInt();
	}

	@Override public boolean focusable() {
		return this.isActive();
	}

	@Override public void render1() {
		this.scroll(0);

		try (var __ = this.pushScissor(this.absoluteX(), this.absoluteY(), this.width(), this.height())) {
			super.render1();
		}
	}

	@Override protected boolean scroll(double amount) {
		var max = this.max();

		if (this.scroll != (this.scroll = (int) MathHelper.clamp(this.scroll - 24 * amount, 0, max))) {
			super.scroll(amount);
		} else if (max == 0) {
			return false;
		}

		return !isControlDown();
	}

	double ratio() {
		return (double) this.scroll / this.max();
	}

	int max() {
		return Math.max(0, this.bodyHeight() - this.height());
	}
}
