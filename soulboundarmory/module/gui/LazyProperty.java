package soulboundarmory.module.gui;

import java.util.function.Function;

public class LazyProperty<T> {
	public Period period = Period.FRAME;
	public Function<LazyProperty<T>, ? extends T> initializer;
	public boolean set;

	private T value;

	public static <T> LazyProperty<T> of(Period period, Function<LazyProperty<T>, ? extends T> initializer) {
		var property = new LazyProperty<T>();
		property.period = period;
		property.initializer = initializer;

		return property;
	}

	public static <T> LazyProperty<T> of(Function<LazyProperty<T>, ? extends T> initializer) {
		return of(Period.FRAME, initializer);
	}

	public T value() {
		if (!this.set) {
			this.set(this.initializer.apply(this));
		}

		return this.value;
	}

	public void set(T value) {
		this.value = value;
		this.set = true;
	}

	public enum Period {
		TICK,
		FRAME
	}
}
