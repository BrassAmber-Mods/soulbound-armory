package soulboundarmory.module.gui.box;

import net.minecraft.util.math.MathHelper;
import soulboundarmory.module.gui.LazyProperty;
import soulboundarmory.module.gui.Length;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.util.Math2;
import soulboundarmory.util.Util;

import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class Side<T extends Widget<T>> {
	public final LazyProperty<Integer> p;
	public final LazyProperty<Integer> pAbsolute;
	public final LazyProperty<Integer> pLength;
	public final Length length = new Length();

	public Type type = Type.RELATIVE;
	public int position;
	public double offset;

	final T node;
	final boolean x;
	final Side<T> main;
	IntSupplier coordinateValue = Util.zeroSupplier;

	Side(T node, boolean x, Side<T> main) {
		this.node = node;
		this.x = x;
		this.main = main == null ? this : main;
		this.p = node.property(p -> this.resolve(true));
		this.pAbsolute = node.property(p -> this.resolve(false));
		this.pLength = node.property(p -> {
			if (main == null) {
				var min = x ? this.node.min.x.pLength.value() : this.node.min.y.pLength.value();
				p.set(min);
				return MathHelper.clamp(this.defaultLength(), min, (x ? this.node.max.x : this.node.max.y).pLength.value());
			}

			return this.defaultLength();
		});
	}

	public T type(Type type) {
		this.type = type;
		return this.node;
	}

	/**
	 Offsets the node at a fractional point along its parent's side.

	 @param value a fraction of the parent's width
	 @return the node
	 */
	public T offset(double value) {
		this.offset = value;
		return this.type(Type.RELATIVE);
	}

	public T position(int position) {
		this.position = position;
		return this.node;
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

	public T center() {
		this.offset(0.5);
		return this.middle();
	}

	public T value(int value) {
		return this.value(() -> value);
	}

	public T value(IntSupplier value) {
		this.coordinateValue = value;
		return this.node;
	}

	public T value(double offset, int value) {
		this.offset(offset);
		return this.value(value);
	}

	public T value(Widget<?> node) {
		this.type = Type.ABSOLUTE;
		return this.value(() -> this.x ? node.absoluteX() : node.absoluteY());
	}

	public int defaultLength() {
		return this.length.value.getAsInt() + (int) switch (this.length.type) {
			case EXACT -> this.length.base().getAsDouble();
			case PARENT_PROPORTION -> this.length.base().getAsDouble() * this.node.parent().map(w -> x ? w.width() : w.height()).orElseGet(() -> this.x ? Widget.windowWidth() : Widget.windowHeight());
			case CHILD_RANGE -> this.x ? this.node.descendantWidth() : this.node.descendantHeight();
		};
	}

	@Override public String toString() {
		return "type %s offset %s position %s coordinate %s".formatted(this.type, this.offset, this.position, this.coordinateValue.getAsInt());
	}

	int resolve(boolean relative) {
		return this.x ? this.resolve(relative, Widget::absoluteX, Widget::width, Widget::windowWidth)
			: this.resolve(relative, Widget::absoluteY, Widget::height, Widget::windowHeight);
	}

	int resolve(boolean relative, Function<Widget<?>, Integer> absolutePosition, Function<Widget<?>, Integer> length, Supplier<Integer> windowLength) {
		return this.coordinateValue.getAsInt()
			+ (this.type == Type.ABSOLUTE || relative || this.node.isRoot() ? 0 : absolutePosition.apply(this.node.parent))
			+ (this.offset == 0 ? 0 : Math2.iround(this.node.parent().map(length).orElseGet(windowLength) * this.offset))
			- (this.position == 0 ? 0 : this.main.pLength.value() / this.position);
	}

	public enum Type {
		ABSOLUTE,
		RELATIVE,
		RELATIVE_FIXED
	}
}
