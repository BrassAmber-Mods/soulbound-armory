package soulboundarmory.client.gui.screen;

import soulboundarmory.client.gui.widget.SelectionEntryWidget;
import soulboundarmory.text.Translations;
import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.config.Configuration;
import soulboundarmory.item.SoulboundItems;
import soulboundarmory.module.gui.widget.ItemWidget;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.module.gui.widget.WidgetBox;
import soulboundarmory.util.ItemUtil;

/**
 The item selection tab, which adds a button for each {@linkplain ItemComponent#canConsume unlockable} item and each {@linkplain ItemComponent#isUnlocked unlocked} item.
 When a button is pressed, {@link ItemComponent#select} is invoked.
 */
public class SelectionTab extends Tab {
	public SelectionTab() {
		super(Translations.guiToolSelection);
	}

	@Override public Widget<?> icon() {
		return new ItemWidget().item(SoulboundItems.greatsword);
	}

	@Override public void initialize() {
		var parent = this.container();
		var component = parent.component;
		var box = this.add(new WidgetBox<>().center().x(0.5).y(0.5));

		switch (Configuration.Client.selectionEntryType) {
			case ICON -> box.xMargin(12);
			case TEXT -> box.yMargin(8);
		}

		component.items.values().stream()
			.filter(ItemComponent::isEnabled)
			.filter(item -> item.unlocked && component.matches(parent.stack) || item.canConsume(parent.stack))
			.forEach(item -> box.add(new SelectionEntryWidget(item))
				.primaryAction(() -> item.select(parent.slot))
				.active(() -> ItemUtil.inventory(player()).noneMatch(item::matches) && (component.cooldown() <= 0 || ItemUtil.inventory(player()).anyMatch(item::canConsume)))
			);
	}
}
