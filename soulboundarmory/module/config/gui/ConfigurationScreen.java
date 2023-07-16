package soulboundarmory.module.config.gui;

import net.minecraft.client.gui.screen.Screen;
import soulboundarmory.module.config.ConfigurationInstance;
import soulboundarmory.module.config.Node;
import soulboundarmory.module.gui.screen.ScreenWidget;
import soulboundarmory.module.gui.widget.ScalableWidget;
import soulboundarmory.module.gui.widget.ScrollWidget;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.module.gui.widget.WidgetBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigurationScreen extends ScreenWidget<ConfigurationScreen> {
	final ConfigurationInstance instance;
	final WidgetBox categories = this.add(new WidgetBox<>().x.center().y(16).xMargin(8));
	final Map<String, Widget<?>> categoryMap = new HashMap<>();
	Widget<?> category;

	public ConfigurationScreen(ConfigurationInstance instance, Screen parent) {
		super(parent);

		this.instance = instance;
		var categories = this.instance.children().collect(Collectors.groupingBy(node -> node.category));
		categories.computeIfPresent(this.instance.category, (category, main) -> {
			this.addCategory(category, main);
			return null;
		});

		categories.forEach((category, nodes) -> this.addCategory(category, nodes));
	}

	public ConfigurationScreen open(String category) {
		this.category = Objects.requireNonNull(this.categoryMap.get(category));
		this.open();

		return this;
	}

	@Override public void reinitialize() {
		keyboard.setRepeatEvents(true);
	}

	@Override protected void render() {
		this.renderBackground(this.instance.background);
		this.drawHorizontalLine(0, this.absoluteEndX(), this.category.absoluteY() - 1, this.z() + 100, 0xFF000000);
	}

	void addCategory(String category, List<Node> nodes) {
		var tab = this.add(new ScrollWidget()).present(c -> c == this.category).height(w -> this.height() - w.y()).y(t -> this.categories.y() + this.categories.descendantHeight(false) + 20)
			.with(new CategoryWidget(nodes, -1));
		if (this.category == null) this.category = tab;
		this.categoryMap.put(category, tab);
		this.categories.add(new ScalableWidget<>().button().text(category).width(b -> Math.max(70, b.descendantWidth() + 10)).height(20).active(w -> !tab.isPresent()).primaryAction(w -> this.category = tab));
	}
}
