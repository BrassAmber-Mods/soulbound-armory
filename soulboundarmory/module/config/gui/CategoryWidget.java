package soulboundarmory.module.config.gui;

import soulboundarmory.module.config.Group;
import soulboundarmory.module.config.Node;
import soulboundarmory.module.config.Property;
import soulboundarmory.module.gui.widget.WidgetBox;

public class CategoryWidget extends WidgetBox<CategoryWidget> {
	public CategoryWidget(Iterable<Node> nodes, int depth) {
		this.vertical();
		this.width(1D);

		for (var node : nodes) {
			if (node instanceof Property<?> property) {
				this.add(new PropertyWidget(property, depth + 1));
			} else if (node instanceof Group group) {
				this.add(new GroupWidget(group, depth + 1));
			}
		}
	}
}
