package cell.client.gui.widget;

import java.text.NumberFormat;
import cell.client.gui.DrawableElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Slider extends AbstractSlider implements DrawableElement {
    protected static final NumberFormat floatFormat = NumberFormat.getNumberInstance();

    public ITextComponent label;
    public ScrollCallback onScroll;

    protected double min;
    protected double max;
    protected double range;
    protected double scaledValue;
    protected double remainder;

    public boolean discrete;

    public Slider() {
        super(0, 0, 0, 0, StringTextComponent.EMPTY, 0);
    }

    public Slider(int x, int y, int width, int height, double value, double min, double max, ITextComponent label) {
        super(x, y, width, height, label, (value - min) / (max - min));

        this.label = label;
        this.min = min;
        this.max = max;
        this.range = max - min;
        this.scaledValue = value;
        this.discrete = true;

        this.func_230972_a_();
    }

    public Slider x(int x) {
        this.x = x;

        return this;
    }

    public Slider y(int y) {
        this.y = y;

        return this;
    }

    public Slider width(int width) {
        this.width = width;

        return this;
    }

    public Slider height(int height) {
        this.height = height;

        return this;
    }

    public Slider label(ITextComponent label) {
        this.label = label;

        return this;
    }

    public double min() {
        return this.min;
    }

    public Slider min(double min) {
        this.min = min;
        this.range = this.max - min;

        return this;
    }

    public double max() {
        return this.max;
    }

    public Slider max(double max) {
        this.max = max;
        this.range = max - this.min;

        return this;
    }

    public double value() {
        return this.scaledValue;
    }

    public Slider value(double value) {
        this.sliderValue = (value - this.min) / this.range;
        this.scaledValue = value;
        this.func_230972_a_();

        return this;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        var addition = Screen.hasShiftDown()
            ? 0.05 * this.range
            : Screen.hasControlDown()
                ? 1
                : 0.01 * this.range;

        if (this.discrete) {
            var newAddition = Math.ceil(Math.abs(addition)) + (long) this.remainder;
            this.remainder += addition - newAddition;
            addition = newAddition;
        }

        this.value(MathHelper.clamp(this.scaledValue + Math.signum(amount) * addition, this.min, this.max));

        return true;
    }

    public void scroll(double amount) {
        // this.value = MathHelper.clamp(this.value + value / 255, 0, 1);
        //
        // this.applyValue();
        // this.updateMessage();

        this.mouseScrolled(0, 0, amount);
    }

    @Override
    protected void func_230972_a_() {
        Object formattedValue = this.discrete || Math.abs(this.scaledValue) >= 100 ? (long) this.scaledValue : floatFormat.format(this.scaledValue);

        this.setMessage(this.label == StringTextComponent.EMPTY ? new StringTextComponent(String.valueOf(formattedValue)) : new StringTextComponent(String.format("%s:%s", this.label, formattedValue)));
    }

    @Override
    protected void func_230979_b_() {
        this.scaledValue = this.min + this.sliderValue * this.range;

        if (this.onScroll != null) {
            this.onScroll.accept(this);
        }
    }

    @Override
    public void tick() {}
}