package soulboundarmory.module.config;

import net.auoeke.reflect.Pointer;
import soulboundarmory.util.Util2;

import java.io.InvalidClassException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class Property<T> extends Node {
	public final Parent parent;
	public final Class<?> type;
	public final String comment;
	public final T defaultValue;
	public final Interval interval;

	final Supplier<Object> get;
	final Consumer<Object> set;

	public Property(Parent parent, Field f, Map<String, T> map, String key, T value) {
		super(key, parent.category);

		if (!(f.getGenericType() instanceof ParameterizedType pt)) throw new InvalidClassException("Map %s.%s is not parameterized".formatted(parent, f.getName()));
		var type = pt.getActualTypeArguments()[1];
		if (!(type instanceof Class || type instanceof ParameterizedType)) throw new InvalidClassException("value argument to type of Map %s.%s is not exact");
		this.type = (Class<?>) (type instanceof ParameterizedType t ? t.getRawType() : type);

		this.parent = parent;
		this.comment = null;
		this.get = () -> map.get(key);
		this.set = v -> map.put(key, (T) v);
		this.defaultValue = value;
		this.interval = null;
	}

	public Property(Parent parent, Field field) {
		super(Util2.value(field, Name::value, field.getName()), fieldCategory(parent, field));

		this.parent = parent;
		this.type = field.getType();
		this.comment = Util2.value(field, (Comment comment) -> String.join("\n", comment.value()));

		var pointer = Pointer.of(field);
		this.get = pointer::get;
		this.set = pointer::put;
		this.defaultValue = this.get();
		this.interval = field.getAnnotation(Interval.class);

		if (this.interval != null && field.getType() != int.class) {
			throw new InvalidClassException("@Interval field %s.%s must be of type int".formatted(parent, field.getName()));
		}

		var quotientInterval = field.getAnnotation(QuotientInterval.class);

		if (quotientInterval != null && field.getType() != double.class) {
			throw new InvalidClassException("@QuotientInterval field %s.%s must be of type double".formatted(parent, field.getName()));
		}
	}

	public ConfigurationInstance configuration() {
		return (ConfigurationInstance) Stream.iterate(this.parent, Objects::nonNull, parent -> parent instanceof Group group ? group.parent : null).reduce(this.parent, (a, b) -> b);
	}

	public T get() {
		return (T) this.get.get();
	}

	public void set(Object value) {
		this.set.accept(value);
		this.configuration().flush = true;
	}

	public void reset() {
		this.set(this.defaultValue);
	}

	@Override public String toString() {
		return this.name + " = " + this.get();
	}
}
