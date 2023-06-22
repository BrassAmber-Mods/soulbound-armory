package soulboundarmory.module.gui.box;

import soulboundarmory.module.gui.Length;
import soulboundarmory.module.gui.widget.Widget;

public class Box<T extends Widget<T>> {
	public final Side<T> x;
	public final Side<T> y;
	public final Length width;
	public final Length height;

	public Box(T node, Box<T> main) {
		if (main == null) main = this;
		this.x = new Side<>(node, true, main.x);
		this.y = new Side<>(node, false, main.y);
		this.width = this.x.length;
		this.height = this.y.length;
	}

	public T type(Side.Type type) {
		this.x.type(type);
		return this.y.type(type);
	}

	public T start() {
		return this.position(0);
	}

	public T middle() {
		return this.position(2);
	}

	public T end() {
		return this.position(1);
	}

	public T position(int position) {
		this.x.position(position);
		return this.y.position(position);
	}
}
