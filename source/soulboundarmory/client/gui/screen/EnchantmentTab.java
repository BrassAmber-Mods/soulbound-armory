package soulboundarmory.client.gui.screen;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import soulboundarmory.client.i18n.Translations;
import soulboundarmory.component.statistics.Category;
import soulboundarmory.component.statistics.StatisticType;
import soulboundarmory.module.gui.widget.*;
import soulboundarmory.network.ExtendedPacketBuffer;
import soulboundarmory.network.Packets;

public class EnchantmentTab extends Tab {
	public EnchantmentTab() {
		super(Translations.guiButtonEnchantments);
	}

	@Override public Widget<?> icon() {
		return new ItemWidget().item(Items.ENCHANTING_TABLE);
	}

	@Override public void initialize() {
		var component = this.container().item();
		var enchantments = component.enchantments;
		this.add(this.resetButton(Category.enchantment)).active(() -> enchantments.values().intStream().anyMatch(level -> level > 0));
		this.displayPoints(() -> this.container().item().intValue(StatisticType.enchantmentPoints));

		var length = Math.max(this.container().xpBar.width(), width(enchantments.reference2IntEntrySet().stream().map(entry -> entry.getKey().getName(entry.getIntValue()))) + 60);
		var box = new WidgetBox<>().yMargin(4).x.center().width(length);
		var scroll = this.add(new ScrollWidget())
			.min.y.value(() -> this.button.absoluteEndY() + 8)
			.y(0.5)
			.y.middle()
			.with(box);

		scroll.max.height.base(() -> this.container().xpBar.y() - 16 - scroll.min.y.p.value());

		enchantments.forEach(enchantment -> box.add(new Widget<>()).width(length).height(20)
			.with(this.squareButton("-", () -> this.enchant(enchantment, false)).x(1, -20).x.end().y.center().active(() -> component.enchantment(enchantment) > 0))
			.with(this.squareButton("+", () -> this.enchant(enchantment, true)).x(1D).x.end().y.center().active(() -> component.enchantmentPoints() > 0))
			.text(widget -> widget.y.center().shadow().text(() -> enchantment.getName(component.enchantment(enchantment))))
		);
	}

	private void enchant(Enchantment enchantment, boolean enchant) {
		Packets.serverEnchant.send(new ExtendedPacketBuffer(this.container().item())
			.writeIdentifier(ForgeRegistries.ENCHANTMENTS.getKey(enchantment))
			.writeBoolean(enchant)
			.writeBoolean(isShiftDown())
		);
	}
}
