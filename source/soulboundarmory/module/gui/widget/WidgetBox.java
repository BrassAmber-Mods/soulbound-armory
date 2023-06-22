package soulboundarmory.module.gui.widget;

public class WidgetBox<T extends WidgetBox<T>> extends Widget<T> {
	public boolean horizontal = true;
	public int margin;

	public WidgetBox() {
		this.width(w -> this.descendantWidth(false));
		this.height(w -> this.descendantHeight(false));
	}

	@Override public <C extends Widget> C add(int index, C child) {
		super.add(index, child);

		var previous = this.degree() == 1 ? null : this.child(this.degree() - 2);
		return this.horizontal ? (C) child.x(previous == null ? c -> 0 : c -> previous.endX() + this.margin)
			: (C) child.y(previous == null ? c -> 0 : c -> previous.endY() + this.margin);
	}

	public T horizontal() {
		this.horizontal = true;
		return (T) this;
	}

	public T vertical() {
		this.horizontal = false;
		return (T) this;
	}

	public T margin(int margin) {
		this.margin = margin;
		return (T) this;
	}

	public T xMargin(int margin) {
		return this.horizontal().margin(margin);
	}

	public T yMargin(int margin) {
		return this.vertical().margin(margin);
	}
}
