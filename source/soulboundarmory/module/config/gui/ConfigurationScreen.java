package soulboundarmory.module.config.gui;

import net.minecraft.client.gui.screen.Screen;
import soulboundarmory.module.config.ConfigurationInstance;
import soulboundarmory.module.config.Node;
import soulboundarmory.module.gui.screen.ScreenWidget;
import soulboundarmory.module.gui.widget.ScalableWidget;
import soulboundarmory.module.gui.widget.WidgetBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigurationScreen extends ScreenWidget<ConfigurationScreen> {
	protected final ConfigurationInstance instance;
	protected final WidgetBox categories = this.add(new WidgetBox<>().centerX().x(0.5).y(16).xSpacing(8));
	protected final Map<String, CategoryWidget> categoryMap = new HashMap<>();
	protected CategoryWidget category;

	public ConfigurationScreen(ConfigurationInstance instance, Screen parent) {
		super(parent);

		this.instance = instance;
		var categories = this.instance.children().collect(Collectors.groupingBy(node -> node.category));
		var main = categories.remove(this.instance.category);

		if (main != null) {
			this.addCategory(this.instance.category, main);
		}

		categories.forEach(this::addCategory);
	}

	public ConfigurationScreen open(String category) {
		this.category = Objects.requireNonNull(this.categoryMap.get(category));
		this.open();

		return this;
	}

	@Override public void preinitialize() {
		keyboard.setRepeatEvents(true);
	}

	@Override protected void render() {
		this.renderBackground(this.instance.background);
		drawHorizontalLine(this.matrixes, 0, this.absoluteEndX(), this.category.absoluteY(), this.z(), 0xFF000000);
	}

	private void addCategory(String category, List<Node> nodes) {
		var tab = this.add(new CategoryWidget(nodes, -1).with(c -> c.present(() -> c == this.category)));
		this.categoryMap.put(category, tab);
		this.category = Objects.requireNonNullElse(this.category, tab);
		var button = this.categories.add(new ScalableWidget<>().button().text(category).width(b -> Math.max(70, b.descendantWidth() + 10)).height(20).active(() -> !tab.isPresent()).primaryAction(() -> this.category = tab));
		tab.y(t -> button.endY() + 20);
	}
}
