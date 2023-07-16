package soulboundarmory.module.config.gui;

import soulboundarmory.module.config.Group;
import soulboundarmory.module.gui.widget.TextWidget;
import soulboundarmory.module.gui.widget.WidgetBox;

public class GroupWidget extends WidgetBox<GroupWidget> {
	private boolean expanded;

	public GroupWidget(Group group, int depth) {
		this.width(w -> windowWidth() - w.absoluteX());
		this.vertical().add(new EntryWidget<>(group.comment)
			.height(32)
			.primaryAction(() -> this.expanded ^= true)
			.with(new TextWidget().text(() -> (this.expanded ? "∨ " : "› ") + group.name).x(8).y.center())
		);
		this.add(new CategoryWidget(group.children().toList(), depth).y(32).present(() -> this.expanded));
	}
}
