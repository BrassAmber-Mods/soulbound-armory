package soulboundarmory.client.gui.screen;

import net.minecraft.text.Text;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import soulboundarmory.text.Translations;
import soulboundarmory.component.statistics.Category;
import soulboundarmory.module.gui.widget.ScalableWidget;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.network.ExtendedPacketBuffer;
import soulboundarmory.network.Packets;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.function.IntSupplier;

@OnlyIn(Dist.CLIENT)
public abstract class Tab extends Widget<Tab> {
	protected static final NumberFormat format = DecimalFormat.getInstance();

	public final Text title;
	public int index;
	public Widget<?> button;

	public Tab(Text title) {
		this.title = title;
	}

	public abstract Widget<?> icon();

	public Widget<?> squareButton(String text, Runnable action) {
		return new ScalableWidget<>()
			.button()
			.center()
			.size(20)
			.text(Text.of(text))
			.primaryAction(action);
	}

	public Widget<?> resetButton(Category category) {
		return new ScalableWidget<>()
			.button()
			.with(new ScalableWidget<>()
				.x(0.5)
				.y(0.5)
				.center()
				.texture("soulboundarmory:textures/gui/counterclockwise_arrow.png")
				.textureSize(16, 16)
				.slice(0, 16, 16, 0, 16, 16)
			)
			.x(this.container().options)
			.y.end()
			.y(15D / 16)
			.size(20)
			.primaryAction(() -> this.reset(category));
	}

	public SoulboundScreen container() {
		return (SoulboundScreen) super.parent;
	}

	protected void reset(Category category) {
		Packets.serverReset.send(new ExtendedPacketBuffer(this.container().item()).writeIdentifier(category.id()));
	}

	protected void displayPoints(IntSupplier points) {
		this.centeredText(widget -> widget.text(() -> this.pointText(points.getAsInt())).y(0, 4));
	}

	protected Text pointText(int points) {
		return points == 0 ? Translations.empty : Translations.guiUnspentPoints.text(points);
	}
}
