package soulboundarmory.module.gui.widget;

import soulboundarmory.module.gui.box.Side;

import java.util.function.Consumer;

public class TooltipWidget extends Widget<TooltipWidget> {
	@Override public <C extends Widget> C add(int index, C child) {
		return (C) super.add(index, child).box.type(Side.Type.ABSOLUTE);
	}

	@Override public TooltipWidget centeredText(Consumer<? super TextWidget> configure) {
		return this.text(configure);
	}

	@Override protected void render() {
		var mouseFocused = this.parent != null && this.parent.mouseFocused;
		this.withZ(() -> this.renderTooltipFromComponents(this.children, mouseFocused ? mouseX() : this.absoluteX() - 8, mouseFocused ? mouseY() : this.absoluteY()));
	}
}
