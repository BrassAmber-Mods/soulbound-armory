package soulboundarmory.module.gui.widget;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.auoeke.reflect.Flags;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL46C;
import soulboundarmory.module.gui.LazyProperty;
import soulboundarmory.module.gui.Length;
import soulboundarmory.module.gui.MatrixTransformation;
import soulboundarmory.module.gui.Scissor;
import soulboundarmory.module.gui.box.Side;
import soulboundarmory.module.gui.box.Box;
import soulboundarmory.module.gui.screen.ScreenDelegate;
import soulboundarmory.module.gui.screen.ScreenWidget;
import soulboundarmory.module.gui.util.function.BiFloatIntConsumer;
import soulboundarmory.module.gui.util.function.ObjectSupplier;
import soulboundarmory.module.gui.widget.callback.PressCallback;
import soulboundarmory.module.gui.widget.scroll.ContextScrollAction;
import soulboundarmory.module.gui.widget.scroll.ScrollAction;
import soulboundarmory.util.Iteratable;
import soulboundarmory.util.Util2;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Stream;

import static org.lwjgl.glfw.GLFW.*;

/**
 A node in a tree of GUI elements.

 @param <T> the type of the node
 */
@OnlyIn(Dist.CLIENT)
public class Widget<T extends Widget<T>> extends DrawableHelper implements Drawable, Element, Cloneable, TooltipComponent {
	public static final MinecraftClient client = MinecraftClient.getInstance();
	public static final Window window = client.getWindow();
	public static final Keyboard keyboard = client.keyboard;
	public static final Mouse mouse = client.mouse;
	public static final TextRenderer textRenderer = client.textRenderer;
	public static final TextureManager textureManager = client.textureManager;
	public static final TextHandler textHandler = textRenderer.getTextHandler();
	public static final ResourceManager resourceManager = client.getResourceManager();
	public static final ItemRenderer itemRenderer = client.getItemRenderer();
	public static final EntityRenderDispatcher entityRenderDispatcher = client.getEntityRenderDispatcher();
	public static final InGameHud hud = client.inGameHud;
	public static final BakedModelManager bakedModelManager = client.getBakedModelManager();
	public static final GameRenderer gameRenderer = client.gameRenderer;
	public static final SoundManager soundManager = client.getSoundManager();

	protected static ReferenceArrayList<Scissor> scissors = ReferenceArrayList.of();

	public Widget<?> parent;
	public ReferenceArrayList<Widget<?>> children = ReferenceArrayList.of();
	public ReferenceArrayList<Widget<?>> tooltips = ReferenceArrayList.of();

	/** The element selected by the keyboard; may be {@code null}, {@code this} or a child. */
	public Optional<Widget<?>> selected = Optional.empty();
	public PressCallback<T> primaryAction;
	public PressCallback<T> secondaryAction;
	public PressCallback<T> tertiaryAction;
	public ContextScrollAction<T> scrollAction;

	public Predicate<T> present = w -> !w.isTooltip() || w.isPresentTooltip();
	public Predicate<T> visible = Predicates.alwaysTrue();
	public Predicate<T> active = Predicates.alwaysTrue();

	/** Is the deepest element that is hovered by the mouse. */
	public boolean mouseFocused;
	public boolean dragging;

	/** Stored by {@link #render(MatrixStack)} in order to avoid passing it around everywhere. */
	public MatrixStack matrixes;

	protected double dragX;
	protected double dragY;

	private final Set<Widget<?>> renderDeferred = ReferenceLinkedOpenHashSet.of();

	public final List<LazyProperty<?>> allProperties = ReferenceArrayList.of();
	public final List<LazyProperty<?>> frameProperties = ReferenceArrayList.of();

	public final Box<T> box = new Box<>((T) this, null);
	public final Box<T> min = new Box<>((T) this, this.box);
	public final Box<T> max = new Box<>((T) this, this.box);
	public final Side<T> x = this.box.x;
	public final Side<T> y = this.box.y;

	protected final Length width = this.x.length;
	protected final Length height = this.y.length;

	public Widget() {
		var max = Integer.MAX_VALUE >> 16;
		var min = Integer.MIN_VALUE >> 16;

		this.min.x.value(min)
			.min.y.value(min)
			.max.x.value(max)
			.max.y.value(max);

		this.min.width.base(0);
		this.min.height.base(0);
		this.max.width.base(max >> 1);
		this.max.height.base(max >> 1);
	}

	public static Screen screen() {
		return client.currentScreen;
	}

	public static ScreenWidget<?> cellScreen() {
		return client.currentScreen instanceof ScreenDelegate screen ? screen.screen : null;
	}

	public static int fontHeight() {
		return textRenderer.fontHeight;
	}

	public static int windowWidth() {
		return window.getScaledWidth();
	}

	public static int windowHeight() {
		return window.getScaledHeight();
	}

	public static int unscale(int scaled) {
		return (int) (scaled * window.getScaleFactor());
	}

	public static float tickDelta() {
		return client.getLastFrameDuration();
	}

	public static ClientPlayerEntity player() {
		return client.player;
	}

	public static boolean isShiftDown() {
		return Screen.hasShiftDown();
	}

	public static boolean isControlDown() {
		return Screen.hasControlDown();
	}

	public static boolean isAltDown() {
		return Screen.hasAltDown();
	}

	public static double mouseX() {
		return mouse.getX() * window.getScaledWidth() / window.getWidth();
	}

	public static double mouseY() {
		return mouse.getY() * window.getScaledHeight() / window.getHeight();
	}

	public static int width(String string) {
		return textRenderer.getWidth(string);
	}

	public static int width(StringVisitable text) {
		return textRenderer.getWidth(text);
	}

	public static int width(Stream<? extends StringVisitable> text) {
		return text.mapToInt(Widget::width).max().orElse(0);
	}

	public static int width(Iterable<? extends StringVisitable> text) {
		return width(Util2.stream(text));
	}

	/** @return whether an area starting at (`startX`, `startY`) with dimensions (`width`, `height`) contains the point (`x`, `y`) */
	public static boolean contains(double x, double y, double startX, double startY, double width, double height) {
		return Util2.contains(x, startX, startX + width) && Util2.contains(y, startY, startY + height);
	}

	public static boolean intersect(double ax0, double ax1, double ay0, double ay1, double bx0, double bx1, double by0, double by1) {
		return (Util2.contains(bx0, ax0, ax1) || Util2.contains(bx1, ax0, ax1))
			&& (Util2.contains(by0, ay0, ay1) || Util2.contains(by1, ay0, ay1));
	}

	public static boolean isPressed(int keyCode) {
		return InputUtil.isKeyPressed(client.getWindow().getHandle(), keyCode);
	}

	public static void shaderTexture(AbstractTexture texture) {
		RenderSystem.setShaderTexture(0, texture.getGlId());
	}

	public static void shaderTexture(Identifier texture) {
		RenderSystem.setShaderTexture(0, texture);
	}

	public static void bind(Identifier texture) {
		textureManager.bindTexture(texture);
	}

	public static void setPositionColorShader() {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
	}

	public static void setPositionColorTextureShader() {
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
	}

	public static void brightness(float brightness) {
		RenderSystem.setShaderColor(brightness, brightness, brightness, -1);
	}

	public static void stroke(float x, float y, int color, int strokeColor, BiFloatIntConsumer draw) {
		draw.accept(x + 1, y, strokeColor);
		draw.accept(x - 1, y, strokeColor);
		draw.accept(x, y + 1, strokeColor);
		draw.accept(x, y - 1, strokeColor);
		draw.accept(x, y, color);
	}

	public static void stroke(float x, float y, int color, BiFloatIntConsumer draw) {
		stroke(x, y, color, 0, draw);
	}

	public static List<StringVisitable> wrap(List<? extends StringVisitable> lines, int width) {
		return lines.stream().map(line -> textHandler.wrapLines(line, width, Style.EMPTY)).flatMap(List::stream).toList();
	}

	public static <T extends Widget<?>> Stream<T> present(Stream<T> widgets) {
		return widgets.filter(T::isPresent);
	}

	public static MatrixTransformation push(MatrixStack matrixes) {
		return new MatrixTransformation(matrixes);
	}

	public AutoCloseable pushScissor(int x, int y, int width, int height) {
		var x1 = x + width;
		var y1 = y + height;

		if (!scissors.isEmpty()) {
			var top = scissors.top();
			x = Math.max(x, top.x0());
			y = Math.max(y, top.y0());
			x1 = Math.min(x1, top.x1());
			y1 = Math.min(y1, top.y1());
		}

		var scissor = new Scissor(x, y, x1, y1);
		scissors.push(scissor);
		scissor.apply(this.matrixes);

		return () -> {
			scissors.pop();

			if (scissors.isEmpty()) disableScissor();
			else scissors.top().apply(this.matrixes);
		};
	}

	public void fill(int x1, int y1, int x2, int y2, float z, int color) {
		if (x1 < x2) {
			var i = x1;
			x1 = x2;
			x2 = i;
		}

		if (y1 < y2) {
			var i = y1;
			y1 = y2;
			y2 = i;
		}

		var a = (color >> 24 & 255) / 255F;
		var r = (color >> 16 & 255) / 255F;
		var g = (color >> 8 & 255) / 255F;
		var b = (color & 255) / 255F;
		var bufferBuilder = Tessellator.getInstance().getBuffer();
		var matrix = this.matrixes.peek().getPositionMatrix();

		setPositionColorShader();
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.defaultBlendFunc();

		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix, x1, y2, z).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, x2, y2, z).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, x2, y1, z).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, x1, y1, z).color(r, g, b, a).next();

		BufferRenderer.drawWithShader(bufferBuilder.end());
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	public void drawHorizontalLine(int x1, int x2, int y, int z, int color) {
		if (x2 < x1) {
			var i = x1;
			x1 = x2;
			x2 = i;
		}

		this.fill(x1, y, x2 + 1, y + 1, z, color);
	}

	public void drawVerticalLine(int x, int y1, int y2, int z, int color) {
		if (y2 < y1) {
			var i = y1;
			y1 = y2;
			y2 = i;
		}

		this.fill(x, y1 + 1, x + 1, y2, z, color);
	}

	/**
	 Draw text with stroke.

	 @param x initial x
	 @param y initial y
	 */
	public void drawStrokedText(Text text, float x, float y, int color, int strokeColor) {
		stroke(x, y, color, strokeColor, (i, j, color1) -> textRenderer.draw(this.matrixes, text, i, j, color1));
	}

	/**
	 Draw text with stroke.

	 @param x initial x
	 @param y initial y
	 */
	public void drawStrokedText(String string, float x, float y, int color, int strokeColor) {
		stroke(x, y, color, strokeColor, (i, j, color1) -> textRenderer.draw(this.matrixes, string, i, j, color1));
	}

	/**
	 Draw text with stroke.

	 @param x initial x
	 @param y initial y
	 */
	public void drawStrokedText(Text text, float x, float y, int color) {
		this.drawStrokedText(text, x, y, color, 0);
	}

	/**
	 Draw text with stroke.

	 @param x initial x
	 @param y initial y
	 */
	public void drawStrokedText(String text, float x, float y, int color) {
		this.drawStrokedText(text, x, y, color, 0);
	}

	public void renderTooltip(List<? extends StringVisitable> lines, double x, double y) {
		screen().renderComponentTooltip(this.matrixes, lines, (int) x, (int) y, textRenderer, ItemStack.EMPTY);
	}

	public void renderTooltip(StringVisitable text, double x, double y) {
		this.renderTooltip(List.of(text), (int) x, (int) y);
	}

	public void renderTooltipFromComponents(List<? extends TooltipComponent> components, double x, double y) {
		screen().renderTooltipFromComponents(this.matrixes, (List<TooltipComponent>) components, (int) x, (int) y);
	}

	@Override public T clone() {
		return (T) super.clone();
	}

	public int x() {
		return MathHelper.clamp((int) this.dragX + this.x.p.value(), this.min.x.p.value(), this.max.x.p.value());
	}

	public int y() {
		return MathHelper.clamp((int) this.dragY + this.y.p.value(), this.min.y.p.value(), this.max.y.p.value())
			- (this.y.type != Side.Type.RELATIVE_FIXED && this.parent instanceof ScrollWidget scroll ? scroll.scroll : 0);
	}

	public int middleX() {
		return this.x() + this.width() / 2;
	}

	public int middleY() {
		return this.y() + this.height() / 2;
	}

	public int endX() {
		return this.x() + this.width();
	}

	public int endY() {
		return this.y() + this.height();
	}

	/**
	 @return this node's current x coordinate
	 */
	public int absoluteX() {
		return MathHelper.clamp((int) this.dragX + this.x.pAbsolute.value(), this.min.x.pAbsolute.value(), this.max.x.pAbsolute.value());
	}

	public void initialize() {}

	public boolean drag(double x, double y) {
		if (this.dragging) {
			this.dragX += x;
			this.dragY += y;

			return true;
		}

		return false;
	}

	public T x(Side.Type type) {
		return this.x.type(type);
	}

	/**
	 Position this widget horizontally at a point—expressed as a fraction of its width—along the parent element offset from its origin.

	 @param value the fraction of the parent's width
	 @return this
	 */
	public T x(double value) {
		return this.x.offset(value);
	}

	public T x(double offset, int x) {
		return this.x.value(offset, x);
	}

	public T x(Widget<?> node) {
		return this.x(w -> node.absoluteX());
	}

	public T y(Side.Type type) {
		return this.y.type(type);
	}

	/**
	 Position this widget vertically offset from the origin of the parent element at a point expressed as a fraction of the parent element's height.

	 @param y y position along the parent element as a fraction of its height
	 @return this
	 */
	public T y(double y) {
		return this.y.offset(y);
	}

	public T y(double offset, int y) {
		return this.y.value(offset, y);
	}

	public T y(Widget<?> node) {
		return this.y(w -> node.absoluteY());
	}

	public T offset(Side.Type type) {
		return this.x(type).y(type);
	}

	public T center() {
		return this.x.middle().y.middle();
	}

	public T alignStart() {
		return this.box.start();
	}

	public T alignEnd() {
		return this.box.end();
	}

	public T present(Predicate<T> predicate) {
		this.present = predicate;
		return (T) this;
	}

	public T present(BooleanSupplier predicate) {
		return this.present(w -> predicate.getAsBoolean());
	}

	public T visible(Predicate<T> predicate) {
		this.visible = predicate;
		return (T) this;
	}

	public T active(Predicate<T> active) {
		this.active = active;
		return (T) this;
	}

	public T active(BooleanSupplier predicate) {
		return this.active(w -> predicate.getAsBoolean());
	}

	public T movable() {
		this.primaryAction(() -> this.dragging = true);
		return (T) this;
	}

	public T text(String text) {
		return this.text(Stream.of(text.split("\n")).map(Text::of).toList());
	}

	public T text(Text text) {
		return this.text(() -> text);
	}

	public T text(StringVisitable text) {
		return this.text(text::getString);
	}

	public T text(ObjectSupplier text) {
		return this.text(() -> Text.of(String.valueOf(text.get())));
	}

	public T text(Supplier<? extends Text> text) {
		return this.centeredText(widget -> widget.text(text));
	}

	public T text(Iterable<? extends Text> text) {
		return this.with(self -> text.forEach(self::text));
	}

	public T text(Consumer<? super TextWidget> configure) {
		return this.with(self -> self.add(new TextWidget().with(configure)));
	}

	public T centeredText(Consumer<? super TextWidget> configure) {
		return this.text(text -> text.center().x(.5).y(.5).with(configure));
	}

	public T parent(Widget<?> parent) {
		this.parent = parent;
		return (T) this;
	}

	public T primaryAction(PressCallback<T> action) {
		this.primaryAction = action;
		return (T) this;
	}

	public T secondaryAction(PressCallback<T> action) {
		this.secondaryAction = action;
		return (T) this;
	}

	public T tertiaryAction(PressCallback<T> action) {
		this.tertiaryAction = action;
		return (T) this;
	}

	public T primaryAction(Runnable action) {
		this.primaryAction = widget -> action.run();
		return (T) this;
	}

	public T secondaryAction(Runnable action) {
		this.secondaryAction = widget -> action.run();
		return (T) this;
	}

	public T tertiaryAction(Runnable action) {
		this.tertiaryAction = widget -> action.run();
		return (T) this;
	}

	public T scrollAction(ContextScrollAction<T> action) {
		this.scrollAction = action;
		return (T) this;
	}

	public T scrollAction(ScrollAction<T> action) {
		return this.scrollAction((ContextScrollAction<T>) action);
	}

	public T tooltip(Widget<?> tooltip) {
		this.tooltips.add(this.add(tooltip));
		return (T) this;
	}

	public T tooltip(Consumer<TooltipWidget> configure) {
		return this.with(self -> self.tooltip(new TooltipWidget().with(configure)));
	}

	public T select(Widget<?> widget) {
		if (widget == this) {
			this.selected = Optional.of(this);
		} else if (widget == null || this.contains(widget)) {
			this.selected = Optional.ofNullable(widget);
		} else {
			throw new NoSuchElementException();
		}

		return (T) this;
	}

	public <C extends Widget> C add(int index, C child) {
		child.parent(this);
		this.children.add(index, child);

		return child;
	}

	public <C extends Widget> C add(C child) {
		return this.add(this.degree(), child);
	}

	public T add(int index, Iterable<? extends Widget<?>> children) {
		for (var child : children) {
			this.add(index++, child);
		}

		return (T) this;
	}

	public T add(Iterable<? extends Widget<?>> children) {
		return this.add(this.degree(), children);
	}

	public T add(int index, Widget<?>... children) {
		for (var child : children) {
			this.add(index++, child);
		}

		return (T) this;
	}

	public T add(Widget<?>... children) {
		return this.add(this.degree(), children);
	}

	public T with(Widget<?> child) {
		this.add(child);
		return (T) this;
	}

	public <C extends Widget> C remove(C child) {
		this.children.remove(child);
		this.tooltips.remove(child);
		child.parent(null);

		return child;
	}

	public boolean remove(Iterable<? extends Widget<?>> children) {
		var removed = false;

		for (var child : children) {
			this.remove(child);
			removed = true;
		}

		return removed;
	}

	public boolean remove(Widget<?>... children) {
		return this.remove(List.of(children));
	}

	public T clear() {
		this.children.clear();
		this.tooltips.clear();

		return (T) this;
	}

	public void clearTooltips() {
		this.children.removeAll(this.tooltips);
		this.tooltips.clear();
	}

	public int replace(Widget<?> original, Widget<?> replacement) {
		var index = original == null ? -1 : original.index();

		if (index >= 0) {
			this.children.set(index, replacement);
			original.parent(null);
			replacement.parent(this);
		}

		return index;
	}

	public int renew(Widget<?> original, Widget<?> replacement) {
		var index = this.replace(original, replacement);

		if (index < 0) {
			this.add(replacement);
		}

		return index;
	}

	public T x(int x) {
		return this.x.value(x);
	}

	public T x(ToIntFunction<T> x) {
		return this.x.value(() -> x.applyAsInt((T) this));
	}

	/**
	 @return the x coordinate of this node's center
	 */
	public int absoluteMiddleX() {
		return this.absoluteX() + this.width() / 2;
	}

	/**
	 @return the x coordinate at the end of this node
	 */
	public int absoluteEndX() {
		return this.absoluteX() + this.width();
	}

	/**
	 @return this node's current y coordinate
	 */
	public int absoluteY() {
		return MathHelper.clamp((int) this.dragY + this.y.pAbsolute.value(), this.min.y.pAbsolute.value(), this.max.y.pAbsolute.value())
			- (this.y.type != Side.Type.RELATIVE_FIXED && this.parent instanceof ScrollWidget scroll ? scroll.scroll : 0);
	}

	public T y(int y) {
		return this.y.value(y);
	}

	public T y(ToIntFunction<T> y) {
		return this.y.value(() -> y.applyAsInt((T) this));
	}

	/**
	 @return the y coordinate of this node's center
	 */
	public int absoluteMiddleY() {
		return this.absoluteY() + this.height() / 2;
	}

	/**
	 @return the y coordinate at the end of this node
	 */
	public int absoluteEndY() {
		return this.absoluteY() + this.height();
	}

	public int z() {
		return this.getZOffset() + this.parent().map(Widget::z).orElse(0);
	}

	public T z(int z) {
		this.setZOffset(z);
		return (T) this;
	}

	public T addZ(int z) {
		return this.z(this.z() + z);
	}

	/** Push a matrix, translate by {@link #z()}, run {@code render} and pop. */
	public void withZ(Runnable render) {
		try (var ms = this.push().translate(0, 0, this.z())) {
			var previousZ = itemRenderer.zOffset;
			itemRenderer.zOffset = this.z();
			render.run();
			itemRenderer.zOffset = previousZ;
		}
	}

	public int width() {
		return this.x.pLength.value();
	}

	public T width(int width) {
		this.width.base(width);
		return (T) this;
	}

	public T width(double width) {
		this.width.base(width);
		return (T) this;
	}

	public T width(ToIntFunction<T> width) {
		this.width.base(() -> width.applyAsInt((T) this));
		return (T) this;
	}

	public T widthProportion(ToDoubleFunction<T> width) {
		this.width.base(() -> width.applyAsDouble((T) this));
		return (T) this;
	}

	public T minWidth(int width) {
		this.min.x.length.base(width);
		return (T) this;
	}

	public int height() {
		return this.y.pLength.value();
	}

	public T height(int height) {
		this.height.base(height);
		return (T) this;
	}

	public T height(double height) {
		this.height.base(height);
		return (T) this;
	}

	public T height(ToIntFunction<T> height) {
		this.height.base(() -> height.applyAsInt((T) this));
		return (T) this;
	}

	public T heightProportion(ToDoubleFunction<T> height) {
		this.height.base(() -> height.applyAsDouble((T) this));
		return (T) this;
	}

	public T minHeight(int height) {
		this.min.y.length.base(height);
		return (T) this;
	}

	public T size(int width, int height) {
		return this.width(width).height(height);
	}

	public T size(int size) {
		return this.size(size, size);
	}

	public void renderGuiItem(Item item, int x, int y) {
		this.renderGuiItem(item.getDefaultStack(), x, y);
	}

	public void renderGuiItem(ItemStack item, int x, int y) {
		this.withZ(() -> itemRenderer.renderGuiItemIcon(item, x, y));
	}

	/** @return the width of the smallest area that subsumes this node's descendants */
	public int descendantWidth(boolean includeTooltips) {
		return switch (this.degree()) {
			case 0 -> 0;
			case 1 -> {
				var c = this.child(0);
				yield c.isPresent() && (includeTooltips || !c.isTooltip()) ? c.width() : 0;
			}
			default -> {
				var descendants = present(this.descendants(includeTooltips ? Predicates.alwaysTrue() : w -> !w.isTooltip())).toList();
				yield descendants.isEmpty() ? 0 : descendants.stream().mapToInt(Widget::absoluteEndX).max().getAsInt() - descendants.stream().mapToInt(Widget::absoluteX).min().getAsInt();
			}
		};
	}

	public int descendantWidth() {
		return this.descendantWidth(true);
	}

	/** @return the height of the smallest area that subsumes this node's descendants */
	public int descendantHeight(boolean includeTooltips) {
		return switch (this.degree()) {
			case 0 -> 0;
			case 1 -> {
				var c = this.child(0);
				yield c.isPresent() && (includeTooltips || !c.isTooltip()) ? c.height() : 0;
			}
			default -> {
				var descendants = present(this.descendants(includeTooltips ? Predicates.alwaysTrue() : w -> !w.isTooltip())).toList();
				yield descendants.isEmpty() ? 0 : descendants.stream().mapToInt(Widget::absoluteEndY).max().getAsInt() - descendants.stream().mapToInt(Widget::absoluteY).min().getAsInt();
			}
		};
	}

	public int descendantHeight() {
		return this.descendantHeight(true);
	}

	/** @return the number of children that this node has */
	public int degree() {
		return this.children.size();
	}

	public boolean empty() {
		return this.degree() == 0;
	}

	/** @return this node's index in the parent element's node list; -1 if this node is the root */
	public int index() {
		return this.parent().map(parent -> parent.children.indexOf(this)).orElse(-1);
	}

	/** @return whether this node is the root */
	public boolean isRoot() {
		return this.parent == null;
	}

	/**
	 Apply a {@link Consumer} with {@code this} to and return {@code this}.

	 @param consumer a consumer to apply with {@code this}
	 @return {@code this}
	 */
	public T with(Consumer<? super T> consumer) {
		consumer.accept((T) this);
		return (T) this;
	}

	/**
	 Apply a {@link Function} to {@code this} and return its result.

	 @param transform a function to apply to {@code this}
	 @param <R> the type of the result
	 @return the result
	 */
	public <R> R transform(Function<? super T, R> transform) {
		return transform.apply((T) this);
	}

	/** @return this node's parent */
	public Optional<? extends Widget<?>> parent() {
		return Optional.ofNullable(this.parent);
	}

	public Optional<? extends Widget<?>> previous() {
		return this.parent().flatMap(parent -> parent.findChild(this.index() - 1));
	}

	public Optional<? extends Widget<?>> next() {
		return this.parent().flatMap(parent -> parent.findChild(this.index() + 1));
	}

	public Optional<? extends Widget<?>> first() {
		return this.findChild(0);
	}

	public Optional<? extends Widget<?>> last() {
		return this.findChild(this.degree() - 1);
	}

	/** @return a stream of this node's children */
	public Stream<? extends Widget<?>> children() {
		return this.children.stream();
	}

	/** @return a stream of this node's children in reverse order */
	public Stream<? extends Widget<?>> childrenReverse() {
		return Util2.reverseStream(this.children);
	}

	public Stream<? extends Widget<?>> presentChildren() {
		return present(this.children());
	}

	public Stream<? extends Widget<?>> presentChildrenReverse() {
		return present(this.childrenReverse());
	}

	/** @return a stream of this node's ancestors */
	public Stream<? extends Widget<?>> ancestors() {
		return this.parent().stream().mapMulti((parent, buffer) -> {
			buffer.accept(parent);
			parent.ancestors().forEach(buffer);
		});
	}

	/** @return a stream of this node's posterity */
	public Stream<? extends Widget<?>> descendants() {
		return this.descendants(Predicates.alwaysTrue());
	}

	public Stream<? extends Widget<?>> descendants(Predicate<Widget<?>> filter) {
		return this.children().filter(filter).mapMulti((w, add) -> {
			add.accept(w);
			w.descendants(filter).forEach(add);
		});
	}

	/** @return a stream of this node's {@linkplain #isHovered hovered} children */
	public Stream<? extends Widget<?>> hoveredChildren() {
		return this.childrenReverse().filter(Widget::isHovered);
	}

	/** @return the currently hovered descendant rooted at this node */
	public Optional<? extends Widget<?>> hoveredDescendant() {
		return this.childrenReverse()
			.map(Widget::hovered)
			.filter(Optional::isPresent)
			.findFirst()
			.map(Optional::get);
	}

	/** @return the currently hovered node starting at this node as the root */
	public Optional<? extends Widget<?>> hovered() {
		return this.hoveredDescendant().or(() -> Optional.ofNullable(this.isHovered() ? Util2.cast(this) : null));
	}

	/** @return a stream of this node's {@linkplain #isFocused focused} children */
	public Stream<? extends Widget<?>> focusedChildren() {
		return this.childrenReverse().filter(Widget::isFocused);
	}

	/** @return the currently focused descendant rooted at this node */
	public Optional<? extends Widget<?>> focusedDescendant() {
		return this.childrenReverse()
			.map(Widget::focused)
			.flatMap(Optional::stream)
			.findFirst();
	}

	/** @return the currently focused node starting at this node as the root */
	public Optional<? extends Widget<?>> focused() {
		return this.focusedDescendant().or(() -> Optional.ofNullable(this.isFocused() ? Util2.cast(this) : null));
	}

	/**
	 Check whether a node is this node's child.

	 @param node the node to test
	 @return whether {@code node} is this node's child
	 */
	public boolean contains(Widget<?> node) {
		return this.children.contains(node);
	}

	/** @return the child node at an index in the list of children. */
	public Widget<?> child(int index) {
		return this.children.get(index);
	}

	public Optional<? extends Widget<?>> findChild(int index) {
		return index < 0 || index >= this.degree() ? Optional.empty() : Optional.of(this.child(index));
	}

	/** @return the root node in this node's hierarchy */
	public Widget root() {
		return this.parent().map(Widget::root).orElse(this);
	}

	/** @return whether this node should be treated as existent */
	public boolean isPresent() {
		return this.present.test((T) this) && (this.parent == null || this.parent.isPresent());
	}

	/** @return whether this node should be rendered */
	public boolean isVisible() {
		return this.visible.test((T) this) && this.isPresent() && (this.parent == null || this.parent.isVisible());
	}

	/** @return whether this node is active */
	public boolean isActive() {
		return this.active.test((T) this) && this.isPresent() && this.parent().filter(parent -> !parent.isActive()).isEmpty();
	}

	public boolean isSelected() {
		return this.selected.filter(this::equals).isPresent();
	}

	public boolean isFocused() {
		return this.mouseFocused || this.isSelected();
	}

	public boolean focusable() {
		return this.isActive() && (this.primaryAction != null || this.secondaryAction != null || this.tertiaryAction != null || !this.tooltips.isEmpty());
	}

	public boolean scrollable() {
		return this.scrollAction != null;
	}

	public boolean isTooltip() {
		return this.parent != null && this.parent.tooltips.contains(this);
	}

	public boolean isPresentTooltip() {
		return this.isTooltip() && this.parent.mouseFocused || this.parent.isSelected() && isControlDown() || this.isFocused();
	}

	public void reinitialize() {
		this.select(null);
		this.clear();
		keyboard.setRepeatEvents(true);
		this.initialize();
	}

	public Optional<Widget<?>> selectedChild() {
		return this.selected.filter(widget -> widget != this);
	}

	/**
	 Select the previous or next {@link #focusable} element.
	 <br><br>
	 If none of this element and its children is selected, then try to select <br>
	 - if {@code forward}, this element; <br>
	 - otherwise, the last child of this element. <br>

	 @return {@code true} if an element has been selected
	 */
	@Override public boolean changeFocus(boolean forward) {
		if (this.isPresent()) {
			var direction = forward ? 1 : -1;
			var degree = this.degree();
			int start;

			if (this.isSelected()) {
				if (forward) {
					start = 0;
				} else if (this.isRoot()) {
					start = degree - 1;
				} else {
					this.select(null);
					return false;
				}
			} else if (this.selected.isPresent()) {
				start = this.selected.get().index();
			} else if (forward && this.focusable()) {
				this.select(this);
				return true;
			} else {
				start = forward ? 0 : degree - 1;
			}

			for (var index = start; (forward || index >= 0) && index < degree; index += direction) {
				if (this.child(index).changeFocus(forward)) {
					this.select(this.child(index));
					return true;
				}
			}

			if (!forward && this.focusable()) {
				this.select(this);
				return true;
			}

			if (this.isRoot()) {
				if (this.focusable()) {
					this.select(this);
					return true;
				}

				for (var index = forward ? 0 : degree - 1; forward ? index < start : index > start && index < degree; index += direction) {
					if (this.child(index).changeFocus(forward)) {
						this.select(this.child(index));
						return true;
					}
				}
			}
		}

		this.select(null);
		return false;
	}

	public void render(MatrixStack matrixes) {
		this.frameProperties.forEach(p -> p.set = false);

		if (this.isPresent()) {
			this.renderDeferred.clear();
			this.matrixes = matrixes;
			this.mouseFocused = false;
			this.render1();
		}
	}

	@Override public void render(MatrixStack matrixes, int mouseX, int mouseY, float delta) {
		this.render(matrixes);
	}

	protected void render1() {
		if (this.isHovered()) {
			mouseFocused:
			if (this.focusable()) {
				for (var ancestor : Iteratable.of(this.ancestors())) {
					if (ancestor.z() > this.z() && ancestor.mouseFocused) {
						break mouseFocused;
					}

					ancestor.mouseFocused = false;
				}

				this.mouseFocused = true;
			}
		}

		if (this.isVisible()) {
			this.render();

			this.children()
				.filter(child -> !child.isTooltip())
				.forEach(child -> child.render(this.matrixes));

			if (this.isRoot()) {
				Stream.concat(this.descendants().filter(Widget::isTooltip), this.renderDeferred.stream())
					.forEach(w -> {
						RenderSystem.clear(GL46C.GL_DEPTH_BUFFER_BIT, false);
						w.render(this.matrixes);
					});
			}
		}
	}

	protected void render() {}

	protected void deferRender() {
		this.root().renderDeferred.add(this);
	}

	public void renderBackground(Identifier background) {
		this.renderBackground(background, 0, 0, windowWidth(), windowHeight());
	}

	public void renderTooltip(List<? extends StringVisitable> lines) {
		this.renderTooltip(lines, mouseX(), mouseY());
	}

	public void renderTooltip(StringVisitable text) {
		this.renderTooltip(text, mouseX(), mouseY());
	}

	public void renderTooltip(double x, double y, List<? extends StringVisitable> lines) {
		this.renderTooltip(lines, x, y);
	}

	public void renderTooltip(double x, double y, StringVisitable text) {
		this.renderTooltip(text, x, y);
	}

	@Override public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrixes, ItemRenderer itemRenderer, int z) {
		this.x(x).y(y).z(z).render(matrixes);
	}

	@Override public int getHeight() {
		return this.height();
	}

	@Override public int getWidth(TextRenderer textRenderer) {
		return this.width();
	}

	protected MatrixTransformation push() {
		return push(this.matrixes);
	}

	protected boolean clicked() {
		return this.isActive() && this.mouseFocused;
	}

	protected void playSound() {
		soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1));
	}

	protected void primaryClick() {}

	protected boolean scroll(double amount) {
		if (this.scrollAction == null) {
			return false;
		}

		this.scrollAction.scroll((T) this, amount);
		return true;
	}

	/**
	 @return whether this node is hovered my the cursor
	 */
	public boolean isHovered() {
		return this.contains(mouseX(), mouseY());
	}

	/**
	 @return whether this node is the first in its hierarchy that is hovered by the cursor
	 */
	public boolean isHoveredFirst() {
		return this.root().hovered().filter(this::equals).isPresent();
	}

	/**
	 @return whether this node's area contains the given point
	 */
	public boolean contains(double x, double y) {
		return contains(x, y, this.absoluteX(), this.absoluteY(), this.width(), this.height());
	}

	/** Invoked every tick. */
	public void tick() {
		if (this.isPresent()) {
			this.children.forEach(Widget::tick);
		}
	}

	@Override public void mouseMoved(double mouseX, double mouseY) {
		if (this.isPresent()) {
			this.children.forEach(child -> child.mouseMoved(mouseX, mouseY));
		}
	}

	/** {@inheritDoc} */
	@Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button <= 2 && this.isPresent()) {
			if (this.childrenReverse().anyMatch(child -> child.mouseClicked(mouseX, mouseY, button))) {
				return true;
			}

			if (this.clicked()) {
				var c = switch (button) {
					case 0 -> this.primaryAction;
					case 1 -> this.secondaryAction;
					default -> this.tertiaryAction;
				};

				if (c != null) {
					c.onPress((T) this);
					this.playSound();

					if (button == 0) {
						this.primaryClick();
					}

					return true;
				}
			}
		}

		return false;
	}

	/** {@inheritDoc} */
	@Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (this.isPresent()) {
			if (this.childrenReverse().anyMatch(child -> child.mouseReleased(mouseX, mouseY, button))) {
				return true;
			}

			if (this.dragging) {
				this.dragging = false;
				return true;
			}
		}

		return false;
	}

	/** {@inheritDoc} */
	@Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.isPresent()) {
			if (this.childrenReverse().anyMatch(child -> child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))) {
				return true;
			}

			return this.dragging && this.drag(deltaX, deltaY);
		}

		return false;
	}

	/** {@inheritDoc} */
	@Override public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (this.isPresent()) {
			if (this.childrenReverse().anyMatch(widget -> widget.mouseScrolled(mouseX, mouseY, amount))) {
				return true;
			}

			return this.isHovered() && this.scroll(amount);
		}

		return false;
	}

	/** {@inheritDoc} */
	@Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.isPresent()) {
			if (keyCode == GLFW_KEY_TAB && this.changeFocus(Flags.none(modifiers, GLFW_MOD_SHIFT)) || this.isPresent() && this.childrenReverse().anyMatch(child -> child.keyPressed(keyCode, scanCode, modifiers))) {
				return true;
			}

			if (this.isSelected() && (keyCode == GLFW_KEY_SPACE || keyCode == GLFW_KEY_ENTER)) {
				var c = Flags.all(modifiers, GLFW_MOD_SHIFT) ? this.secondaryAction
					: Flags.all(modifiers, GLFW_MOD_CONTROL) ? this.tertiaryAction
					: this.primaryAction;

				if (c != null) {
					c.onPress((T) this);
					this.playSound();

					return true;
				}
			}
		}

		return false;
	}

	/** {@inheritDoc} */
	@Override public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		return this.isPresent() && this.childrenReverse().anyMatch(child -> child.keyReleased(keyCode, scanCode, modifiers));
	}

	/** {@inheritDoc} */
	@Override public boolean charTyped(char character, int modifiers) {
		return this.isPresent() && this.childrenReverse().anyMatch(child -> child.charTyped(character, modifiers));
	}

	/** {@inheritDoc} */
	@Override public boolean isMouseOver(double mouseX, double mouseY) {
		return this.isPresent() && this.contains(mouseX, mouseY);
	}

	public void renderBackground(Identifier identifier, int x, int y, int width, int height, int value, int alpha) {
		var tessellator = Tessellator.getInstance();
		var buffer = tessellator.getBuffer();
		float f = 32;
		float endX = x + width;
		float endY = y + height;

		shaderTexture(identifier);
		setPositionColorTextureShader();
		RenderSystem.setShaderColor(1, 1, 1, 1);

		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
		buffer.vertex(x, endY, this.z() - 1000).color(value, value, value, 255).texture(0, endY / f + alpha).next();
		buffer.vertex(endX, endY, this.z() - 1000).color(value, value, value, 255).texture(endX / f, endY / f + alpha).next();
		buffer.vertex(endX, y, this.z() - 1000).color(value, value, value, 255).texture(endX / f, alpha).next();
		buffer.vertex(x, y, this.z() - 1000).color(value, value, value, 255).texture(0, alpha).next();
		tessellator.draw();
	}

	public void renderBackground(Identifier identifier, int x, int y, int width, int height, int value) {
		this.renderBackground(identifier, x, y, width, height, value, 0);
	}

	public void renderBackground(Identifier identifier, int x, int y, int width, int height) {
		this.renderBackground(identifier, x, y, width, height, 64, 0);
	}

	public void renderBackground() {
		screen().renderBackground(this.matrixes);
	}

	public <P> LazyProperty<P> property(LazyProperty<P> property) {
		this.allProperties.add(property);
		if (property.period == LazyProperty.Period.FRAME) this.frameProperties.add(property);

		return property;
	}

	public <P> LazyProperty<P> property(LazyProperty.Period period, Function<LazyProperty<P>, ? extends P> initializer) {
		return this.property(LazyProperty.of(period, initializer));
	}

	public <P> LazyProperty<P> property(Function<LazyProperty<P>, ? extends P> initializer) {
		return this.property(LazyProperty.Period.FRAME, initializer);
	}

	@Override public String toString() {
		return this.parent == null ? this.toLocalString() : "%s : %s".formatted(this.toLocalString(), this.parent.toLocalString());
	}

	protected int resolve(Length length, int parent) {
		return length.value.getAsInt() + (int) switch (length.type) {
			case EXACT -> length.base().getAsDouble();
			case PARENT_PROPORTION -> length.base().getAsDouble() * parent;
			default -> throw new IllegalArgumentException();
		};
	}

	 protected String toLocalString() {
		return "%s (%s (%s) + %s, %s (%s) + %s)".formatted(this.getClass().getSimpleName().replaceFirst("(?<=.)Widget|Widget(?=.)", ""), this.x(), this.absoluteX(), this.width(), this.y(), this.absoluteY(), this.height());
	}
}
