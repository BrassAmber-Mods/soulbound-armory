package soulboundarmory.client.gui.screen;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.function.IntSupplier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import soulboundarmory.client.i18n.Translations;
import soulboundarmory.component.statistics.Category;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.module.gui.widget.ScalableWidget;
import soulboundarmory.network.ExtendedPacketBuffer;
import soulboundarmory.network.Packets;

@OnlyIn(Dist.CLIENT)
public abstract class SoulboundTab extends Widget<SoulboundTab> {
    protected static final NumberFormat format = DecimalFormat.getInstance();

    public final Text title;
    public int index;
    public Widget<?> button;

    public SoulboundTab(Text title) {
        this.title = title;
    }

    @Override
    public SoulboundTab parent(Widget<?> parent) {
        if (parent != null) {
            this.width(parent.width()).height(parent.height());
        }

        return super.parent(parent);
    }

    public int top(int rows) {
        return this.top(24, rows);
    }

    public int top(int separation, int rows) {
        return this.absoluteMiddleY() - (rows - 1) * separation / 2;
    }

    public int height(int rows, int row) {
        return this.height(24, rows, row);
    }

    public int height(int separation, int rows, int row) {
        return this.top(rows, separation) + row * separation;
    }

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
            .alignEnd()
            .x(23D / 24)
            .y(15D / 16)
            .width(112)
            .height(20)
            .text(Translations.guiButtonReset)
            .primaryAction(() -> this.reset(category));
    }

    public SoulboundScreen container() {
        return (SoulboundScreen) super.parent.get();
    }

    protected void reset(Category category) {
        Packets.serverReset.send(new ExtendedPacketBuffer(this.container().item()).writeIdentifier(category.id()));
    }

    protected void displayPoints(IntSupplier points) {
        this.centeredText(widget -> widget.text(() -> this.pointText(points.getAsInt())).y(0, 4).alignUp());
    }

    protected Text pointText(int points) {
        return switch (points) {
            case 0 -> LiteralText.EMPTY;
            case 1 -> Translations.guiUnspentPoint;
            default -> Translations.guiUnspentPoints.format(points);
        };
    }
}
