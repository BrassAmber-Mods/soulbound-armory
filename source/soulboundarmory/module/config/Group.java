package soulboundarmory.module.config;

import net.auoeke.reflect.Accessor;
import net.auoeke.reflect.Classes;
import soulboundarmory.util.Util2;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;

public class Group extends Parent {
	public final Parent parent;
	public final boolean flat;
	public final String comment;

	public Group(Parent parent, Class<?> type) {
		super(type, Util2.value(type, Name::value, type.getSimpleName()).toLowerCase(Locale.ROOT), Util2.value(type, (Category category) -> {
			if (!type.getDeclaringClass().isAnnotationPresent(ConfigurationFile.class)) {
				throw new IllegalArgumentException("@Category found on 2+ level nested " + type);
			}

			return category.value();
		}, parent.category));

		this.parent = parent;
		this.flat = type.isAnnotationPresent(Flat.class);
		this.comment = Util2.value(type, (Comment comment) -> String.join("\n", comment.value()));
	}

	public Group(Parent parent, Field field) {
		super(field.getName(), fieldCategory(parent, field));

		this.parent = parent;
		this.flat = field.isAnnotationPresent(Flat.class);
		this.comment = Util2.value(field, (Comment comment) -> String.join("\n", comment.value()));

		if (field.getType() != Map.class) {
			throw new IllegalArgumentException("%s %s.%s is not a Map".formatted(field.getType(), field.getDeclaringClass().getName(), field.getName()));
		}

		Classes.initialize(field.getDeclaringClass());
		var map = (Map<String, Object>) Accessor.getReference(field);
		map.forEach((key, value) -> this.children.put(key, new Property<>(this, field, map, key, value)));
	}
}
