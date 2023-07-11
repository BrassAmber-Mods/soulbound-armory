package soulboundarmory.client.gui.screen;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import soulboundarmory.client.gui.bar.BarStyle;
import soulboundarmory.client.gui.bar.ExperienceBar;
import soulboundarmory.client.i18n.Translations;
import soulboundarmory.client.keyboard.GUIKeyBinding;
import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.component.soulbound.item.armor.ArmorComponent;
import soulboundarmory.component.soulbound.player.MasterComponent;
import soulboundarmory.config.Configuration;
import soulboundarmory.module.config.ConfigurationManager;
import soulboundarmory.module.gui.screen.ScreenWidget;
import soulboundarmory.module.gui.widget.ScalableWidget;
import soulboundarmory.module.gui.widget.TooltipWidget;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.module.gui.widget.WidgetBox;
import soulboundarmory.module.gui.widget.slider.SliderWidget;
import soulboundarmory.network.ExtendedPacketBuffer;
import soulboundarmory.network.Packets;
import soulboundarmory.util.Util;

import java.util.List;
import java.util.function.Supplier;

/**
 The main menu of this mod.
 It keeps track of 4 tabs and stores the currently open tab as its child for rendering and input event handling.
 */
public class SoulboundScreen extends ScreenWidget<SoulboundScreen> {
	public final ExperienceBar xpBar = new ExperienceBar()
		.x(.5)
		.y(1D, -27)
		.center()
		.tooltip(tooltip -> tooltip.text(() -> {
			var xp = (int) this.item.experience();
			return this.item.canLevelUp() ? Translations.barXP.text(xp, this.item.nextLevelXP()) : Translations.barFullXP.text(xp);
		})).primaryAction(() -> Configuration.Client.displayOptions ^= true)
		.secondaryAction(() -> Configuration.Client.overlayExperienceBar ^= true)
		.scrollAction(amount -> this.cycleStyle((int) amount))
		.present(this::displayTabs);

	protected final WidgetBox<?> options = new WidgetBox<>()
		.x(box -> Math.round(this.width() * 23 / 24F) - 100)
		.y(box -> this.optionY(0))
		.yMargin(4)
		.present(() -> Configuration.Client.displayOptions && this.displayTabs())
		.add(
			this.colorSlider(Translations.red, 0),
			this.colorSlider(Translations.green, 1),
			this.colorSlider(Translations.blue, 2),
			this.colorSlider(Translations.alpha, 3),
			this.optionButton(() -> Translations.style.text(Configuration.Client.Bar.style.text), () -> this.cycleStyle(1), () -> this.cycleStyle(-1)),
			this.optionButton(() -> Translations.configure, () -> ConfigurationManager.instance(Configuration.class).screen(this.asScreen()).open("client"), null)
		);
	protected final MasterComponent<?> component;
	protected final int slot;
	protected ItemStack stack;

	private final List<Tab> tabs = ReferenceArrayList.of();
	private ItemComponent<?> item;
	private Tab tab;

	public SoulboundScreen(MasterComponent<?> component, int slot) {
		this.component = component;
		this.slot = slot;
	}

	@Override public void tick() {
		var previousItem = this.item;
		this.baseTick();

		if (previousItem != null && this.item != previousItem) {
			if (this.item == null) {
				this.close();

				return;
			}

			if (this.item.master != this.component) {
				this.close();
				this.item.master.tryOpenGUI(this.stack, this.slot);

				return;
			}

			this.refresh();
		}

		super.tick();
	}

	@Override public void initialize() {
		this.tabs.clear();
		this.baseTick();
		this.add(this.xpBar);

		if (this.displayTabs()) {
			this.tabs.addAll(this.item.tabs());
			this.tab = this.tabs.get(MathHelper.clamp(this.component.tab, 0, this.tabs.size() - 1));
		} else {
			this.tabs.add(this.tab = this.component.selectionTab());
		}

		var tabBox = this.add(new WidgetBox<>()).x.center().y(1D / 16).xMargin(12);

		Util.enumerate(this.tabs, (tab, index) -> {
			tab.index = index;
			tab.button = tabBox.add(this.button(tab));
		});

		this.tab(this.tab);

		if (!(this.item instanceof ArmorComponent)) {
			this.add(new ScalableWidget<>())
				.button()
				.alignEnd()
				.x(w -> this.options.x() + 100)
				.y(15D / 16)
				.width(button -> this.tab instanceof AttributeTab || this.tab instanceof EnchantmentTab ? 80 : 100)
				.height(20)
				.text(() -> this.bound() ? Translations.guiButtonUnbind : Translations.guiButtonBind)
				.present(this::displayTabs)
				.primaryAction(() -> Packets.serverBindSlot.send(new ExtendedPacketBuffer(this.component).writeInt(this.bound() ? -1 : this.slot)));
		}

		this.add(this.options);
	}

	@Override public boolean mouseScrolled(double x, double y, double d) {
		if (super.mouseScrolled(x, y, d)) {
			return true;
		}

		if (d != 0) {
			var tab = this.tabs.get(MathHelper.clamp((int) (this.tab.index - d), 0, this.tabs.size() - 1));

			if (tab != this.tab) {
				this.tab(tab);
				return true;
			}
		}

		return false;
	}

	@Override public boolean shouldClose(int keyCode, int scanCode, int modifiers) {
		return super.shouldClose(keyCode, scanCode, modifiers) || modifiers == 0 && GUIKeyBinding.instance.matchesKey(keyCode, scanCode);
	}

	public ItemComponent<?> item() {
		return this.item;
	}

	public boolean displayTabs() {
		return this.item != null;
	}

	public void refresh() {
		this.reinitialize();
		this.tab.reinitialize();
	}

	private void baseTick() {
		this.stack = player().getInventory().getStack(this.slot);
		this.xpBar.item(this.item = ItemComponent.of(player(), this.stack).orElse(null));
	}

	private boolean bound() {
		return this.component.boundSlot == this.slot;
	}

	private int optionY(int row) {
		return this.height() / 16 + 28 * row;
	}

	private <T extends ScalableWidget<T>> T optionButton(Supplier<? extends Text> text, Runnable primaryAction, Runnable secondaryAction) {
		return new ScalableWidget<T>()
			.button()
			.width(100)
			.height(20)
			.centeredText(widget -> widget.text(text))
			.primaryAction(primaryAction)
			.secondaryAction(secondaryAction);
	}

	private SliderWidget colorSlider(Text text, int id) {
		return new SliderWidget()
			.width(100)
			.height(20)
			.min(0)
			.max(255)
			.discrete()
			.value(Configuration.Client.Bar.get(id))
			.text(text)
			.onSlide(slider -> {
				Configuration.Client.Bar.set(id, (int) slider.value());
				ConfigurationManager.instance(Configuration.class).flush = true;
			});
	}

	private Widget<?> button(Tab tab) {
		return new ScalableWidget<>()
			.grayRectangle()
			.size(32)
			.with(tab.icon()
				.width(0.75)
				.height(0.75)
				.x(0.5)
				.y(0.5)
				.center()
			)
			.tooltip(new TooltipWidget().text(tab.title))
			.primaryAction(() -> this.tab(tab))
			.present(this::displayTabs)
			.active(w -> tab != this.tab);
	}

	private void tab(Tab tab) {
		this.renew(this.tab, this.tab = tab);
		tab.reinitialize();
		this.component.tab(tab.index);
	}

	private void cycleStyle(int change) {
		var index = (Configuration.Client.Bar.style.ordinal() + change) % BarStyle.count;
		Configuration.Client.Bar.style = BarStyle.styles.get(index >= 0 ? index : index + BarStyle.count);
	}
}
