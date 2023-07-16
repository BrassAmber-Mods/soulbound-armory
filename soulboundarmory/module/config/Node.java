package soulboundarmory.module.config;

import soulboundarmory.util.Util2;

import java.lang.reflect.Field;
import java.util.Objects;

public class Node {
	public final String name;
	public final String category;

	public Node(String name, String category) {
		this.name = Objects.requireNonNull(name);
		this.category = Objects.requireNonNull(category);
	}

	static String fieldCategory(Parent parent, Field field) {
		return Util2.value(field, (Category category) -> {
			if (field.getDeclaringClass().isAnnotationPresent(ConfigurationFile.class)) {
				return category.value();
			}

			throw new IllegalArgumentException("@Category found on field %s.%s below the top level".formatted(field.getDeclaringClass().getName(), field.getName()));
		}, parent.category);
	}
}
