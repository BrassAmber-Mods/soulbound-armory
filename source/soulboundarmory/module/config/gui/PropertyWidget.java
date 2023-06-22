package soulboundarmory.module.config.gui;

import net.auoeke.reflect.Types;
import soulboundarmory.module.config.Property;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.module.gui.widget.slider.SliderWidget;

public class PropertyWidget extends EntryWidget<PropertyWidget> {
	private final Property<?> property;
	private final Widget<?> value;

	public PropertyWidget(Property<?> property, int depth) {
		super(property.comment);

		this.property = property;
		this.text(text -> text.text(this.property.name).y.center().x(8 + 24 * depth));

		if (Types.equals(property.type, int.class)) {
			if (property.interval == null) {
				this.value = new IntegerPropertyWidget((Property<Integer>) property).width(100).height(11);
			} else {
				this.value = new SliderWidget().width(100).height(20).discrete().min(property.interval.min()).max(property.interval.max()).value((int) property.get()).onSlide(slider -> property.set((int) slider.value()));
			}
		} else if (Types.equals(property.type, double.class)) {
			this.value = new DoublePropertyWidget((Property<Double>) property).width(100).height(11);
		} else if (Types.equals(property.type, boolean.class)) {
			this.value = new BooleanPropertyWidget((Property<Boolean>) property).width(100).height(20);
		} else if (property.type.isEnum()) {
			this.value = new EnumPropertyWidget((Property<Enum<?>>) property).width(p -> Math.max(100, p.descendantWidth(false) + 10)).height(20);
		} else {
			this.value = null;
			return;
		}

		this.add(this.value.x(1, -8).x.end().y.center());
	}
}
