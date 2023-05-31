package soulboundarmory.module.gui.widget;

public class WidgetBox<T extends WidgetBox<T>> extends Widget<T> {
	public int xSpacing;
	public int ySpacing;
	public boolean horizontal = true;

	@Override public int width() {
		return this.horizontal ? Math.max(this.minWidth, this.children().filter(Widget::isVisible).mapToInt(Widget::width).sum() + this.xSpacing * Math.max(0, this.degree() - 1)) : super.width();
	}

	@Override public int height() {
		return this.horizontal ? super.height() : Math.max(this.minHeight, this.children.stream().filter(Widget::isVisible).mapToInt(Widget::height).sum() + this.ySpacing * Math.max(0, this.degree() - 1));
	}

	@Override public <C extends Widget> C add(int index, C child) {
		return this.update(super.add(index, child));
	}

	public T xSpacing(int spacing) {
		this.xSpacing = spacing;
		return this.horizontal();
	}

	public T ySpacing(int spacing) {
		this.ySpacing = spacing;
		return this.vertical();
	}

	public T horizontal() {
		this.horizontal = true;
		return (T) this;
	}

	public T vertical() {
		this.horizontal = false;
		return (T) this;
	}

	protected <C extends Widget<?>> C update(C child) {
		var previous = this.degree() < 2 ? null : this.children.get(this.degree() - 2);

		return this.horizontal ? (C) child.x(previous == null ? c -> 0 : c -> previous.endX() + this.xSpacing)
			: (C) child.y(previous == null ? c -> 0 : c -> previous.endY() + this.ySpacing);
	}
}
