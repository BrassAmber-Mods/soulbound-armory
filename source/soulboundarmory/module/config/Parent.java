package soulboundarmory.module.config;

import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import net.auoeke.reflect.Fields;
import net.auoeke.reflect.Flags;
import soulboundarmory.util.Util2;

import java.util.Map;
import java.util.stream.Stream;

public abstract class Parent extends Node {
	public final Map<String, Node> children = new Object2ReferenceLinkedOpenHashMap<>();

	public Parent(Class<?> type, String name, String category) {
		super(name, category);

		Stream.concat(
			Fields.staticOf(type)
				.filter(field -> !Flags.any(field, Flags.FINAL | Flags.TRANSIENT))
				.map(field -> field.getType() == Map.class ? new Group(this, field) : new Property<>(this, field)),
			Stream.of(type.getDeclaredClasses()).filter(inner -> Flags.isStatic(inner) && !inner.isAnnotationPresent(ConfigurationFile.class)).map(t -> new Group(this, t))
		).forEach(node -> this.children.put(node.name, node));
	}

	Parent(String name, String category) {
		super(name, category);
	}

	public Stream<Node> category(String category) {
		return this.children.values().stream().filter(child -> child.category.equals(category));
	}

	public Stream<Node> children() {
		return this.children.values().stream().flatMap(node -> node instanceof Group group && group.flat ? group.children() : Stream.of(node));
	}

	@Override public String toString() {
		return this instanceof ConfigurationInstance c ? c.type.getName()
			: this instanceof Group g ? "%s.%s".formatted(g.parent, g.name)
			: Util2.hurl(new Error(this.getClass().getName()));
	}
}
