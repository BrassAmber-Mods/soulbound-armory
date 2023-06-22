package soulboundarmory.module.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.util.Identifier;
import soulboundarmory.module.gui.Length;
import soulboundarmory.module.gui.util.Rectangle;
import soulboundarmory.util.Util2;

/**
 A textured widget that supports 9-slice scaling.
 */
@SuppressWarnings("unused")
public class ScalableWidget<T extends ScalableWidget<T>> extends Widget<T> {
	private static final Identifier widgetsID = new Identifier("textures/gui/advancements/widgets.png");
	private static final Identifier windowID = new Identifier("textures/gui/advancements/window.png");

	public Rectangle[] middles = Util2.fill(new Rectangle[5], Rectangle::new);
	public Rectangle[] corners = Util2.fill(new Rectangle[4], Rectangle::new);
	public AbstractTexture texture;
	public ScaleMode scaleMode = ScaleMode.SLICE;
	public int u, v;
	public float r = 1;
	public float g = 1;
	public float b = 1;
	public float a = 1;

	protected int textureWidth = 256;
	protected int textureHeight = 256;
	protected Length viewWidth = new Length();
	protected Length viewHeight = new Length();

	public T texture(AbstractTexture texture) {
		this.texture = texture;
		return (T) this;
	}

	public T texture(Identifier id) {
		var texture = textureManager.getTexture(id);

		if (texture == null) {
			texture = new ResourceTexture(id);
			textureManager.registerTexture(id, texture);
		}

		return this.texture(texture);
	}

	public T texture(String id) {
		return this.texture(new Identifier(id));
	}

	public T u(int u) {
		this.u = u;
		return (T) this;
	}

	public T v(int v) {
		this.v = v;
		return (T) this;
	}

	public T uv(int u, int v) {
		return this.u(u).v(v);
	}

	public T scaleMode(ScaleMode mode) {
		this.scaleMode = mode;
		return (T) this;
	}

	/**
	 Slice the texture into 9 parts: 4 non-scalable corners and 5 resizable other parts.
	 <pre>
	 - │ + │ -
	 ──┼───┼──
	 + │ + │ +
	 ──┼───┼──
	 - │ + │ -

	 -: non-repeatable corner
	 +: repeatable non-corner
	 ┼: slice marker
	 </pre>

	 @param u0 u coordinate just after a left corner
	 @param u1 u coordinate just before a right corner
	 @param u2 u coordinate just after a right corner
	 @param v0 v coordinate just after a top corner
	 @param v1 v coordinate just before a bottom corner
	 @param v2 v coordinate just after a bottom corner
	 @return {@code this}
	 */
	public T slice(int u0, int u1, int u2, int v0, int v1, int v2) {
		this.corners = new Rectangle[]{
			new Rectangle(0, 0, u0, v0), // top left
			new Rectangle(u1, 0, u2, v0), // top right
			new Rectangle(0, v1, u0, v2), // bottom left
			new Rectangle(u1, v1, u2, v2) // bottom right
		};

		this.middles = new Rectangle[]{
			new Rectangle(u0, 0, u1, v0), // top
			new Rectangle(0, v0, u0, v1), // left
			new Rectangle(u0, v0, u1, v1), // center
			new Rectangle(u1, v0, u2, v1), // right
			new Rectangle(u0, v1, u1, v2) // bottom
		};

		return (T) this;
	}

	public int textureWidth() {
		return this.textureWidth;
	}

	public int textureHeight() {
		return this.textureHeight;
	}

	public T textureWidth(int width) {
		this.textureWidth = width;
		return (T) this;
	}

	public T textureHeight(int height) {
		this.textureHeight = height;
		return (T) this;
	}

	public T textureSize(int width, int height) {
		return this.textureWidth(width).textureHeight(height);
	}

	@Override public int width() {
		return super.width();
		// return this.resolve(this.width, this.parent().map(Widget::width).orElseGet(Widget::windowWidth));
		// return this.resolve(this.height, this.textureWidth);
	}

	@Override public int height() {
		return super.height();
		// return this.resolve(this.height, this.parent().map(Widget::height).orElseGet(Widget::windowHeight));
		// return this.resolve(this.height, this.textureHeight);
	}

	public T fullView() {
		return this.viewWidth(1D).viewHeight(1D);
	}

	public int viewWidth() {
		return this.resolve(this.viewWidth, this.width());
	}

	public int viewHeight() {
		return this.resolve(this.viewHeight, this.height());
	}

	public T viewWidth(int width) {
		this.viewWidth.base(width);
		return (T) this;
	}

	public T viewWidth(double width) {
		this.viewWidth.base(width);
		return (T) this;
	}

	public T viewHeight(int height) {
		this.viewHeight.base(height);
		return (T) this;
	}

	public T viewHeight(double height) {
		this.viewHeight.base(height);
		return (T) this;
	}

	public T view(int width, int height) {
		return this.viewWidth(width).viewHeight(height);
	}

	public T view(double width, double height) {
		return this.viewWidth(width).viewHeight(height);
	}

	public T color4f(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;

		return (T) this;
	}

	public T color3f(float r, float g, float b) {
		return this.color4f(r, g, b, 1);
	}

	public T color3f(float value) {
		return this.color3f(value, value, value);
	}

	public T yellowRectangle() {
		return this.longRectangle(0);
	}

	public T blueRectangle() {
		return this.longRectangle(1);
	}

	public T grayRectangle() {
		return this.longRectangle(2);
	}

	public T yellowSpikedRectangle() {
		return this.spikedRectangle(0);
	}

	public T yellowRoundedRectangle() {
		return this.roundedRectangle(0);
	}

	public T whiteRectangle() {
		return this.rectangle(1);
	}

	public T whiteSpikedRectangle() {
		return this.spikedRectangle(1);
	}

	public T whiteRoundedRectangle() {
		return this.roundedRectangle(1);
	}

	public T slider() {
		return this.button(0);
	}

	public T button() {
		return this.button(1);
	}

	public T window() {
		return this.texture(windowID).slice(14, 238, 252, 22, 126, 140);
	}

	public T longRectangle(int index) {
		return this.texture(widgetsID).v(3 + index * 26).slice(5, 195, 200, 5, 15, 20);
	}

	public T rectangle(int index) {
		return this.texture(widgetsID).uv(1, 129 + index * 26).slice(6, 18, 24, 6, 18, 24);
	}

	public T spikedRectangle(int index) {
		return this.texture(widgetsID).uv(26, 128 + index * 26).slice(10, 16, 26, 10, 16, 26);
	}

	public T roundedRectangle(int index) {
		return this.texture(widgetsID).uv(54, 129 + index * 26).slice(7, 15, 22, 6, 21, 26);
	}

	public T button(int index) {
		return this.texture(ClickableWidget.WIDGETS_TEXTURE).v(46 + index * 20).slice(5, 195, 200, 5, 15, 20);
	}

	public T experienceBar() {
		return this.texture(GUI_ICONS_TEXTURE).v(64).slice(1, 138, 182, 1, 4, 5);
	}

	@Override protected void render() {
		try (var __ = this.pushScissor(this.absoluteX(), this.absoluteY(), this.viewWidth(), this.viewHeight())) {
			RenderSystem.enableBlend();
			shaderTexture(this.texture);
			this.resetColor();

			switch (this.scaleMode) {
				case SLICE -> {
					// corners (not scalable)

					for (var index = 0; index < this.corners.length; ++index) {
						var corner = this.corners[index];
						var width = corner.width();
						var height = corner.height();

						drawTexture(
							this.matrixes,
							this.absoluteX() + index % 2 * (this.width() - corner.width()),
							this.absoluteY() + index / 2 * (this.height() - corner.height()),
							this.z(),
							this.u + corner.x0(),
							this.v + corner.y0(),
							width,
							height,
							this.textureWidth(),
							this.textureHeight()
						);
					}

					// middles (scalable parts)

					var tessellator = Tessellator.getInstance();
					var buffer = tessellator.getBuffer();
					var matrix = this.matrixes.peek().getPositionMatrix();

					for (var index = 0; index < this.middles.length; ++index) {
						record Line(int start, int end) {}

						var middle = this.middles[index];

						var x = switch (index) {
							case 1 -> new Line(this.absoluteX(), this.absoluteX() + middle.width());
							case 3 -> new Line(this.absoluteEndX() - middle.width(), this.absoluteEndX());
							default -> new Line(this.absoluteX() + this.middles[1].width(), this.absoluteEndX() - this.middles[3].width());
						};

						var y = switch (index) {
							case 0 -> new Line(this.absoluteY(), this.absoluteY() + middle.height());
							case 4 -> new Line(this.absoluteEndY() - middle.height(), this.absoluteEndY());
							default -> new Line(this.absoluteY() + this.middles[0].height(), this.absoluteEndY() - this.middles[4].height());
						};

						var textureWidth = (float) this.textureWidth();
						var textureHeight = (float) this.textureHeight();
						var u = (this.u + middle.x0()) / textureWidth;
						var v = (this.v + middle.y0()) / textureHeight;
						var endU = u + Math.min(x.end - x.start, middle.width()) / textureWidth;
						var endV = v + Math.min(y.end - y.start, middle.height()) / textureHeight;
						buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
						buffer.vertex(matrix, x.start, y.end, this.z()).texture(u, endV).next();
						buffer.vertex(matrix, x.end, y.end, this.z()).texture(endU, endV).next();
						buffer.vertex(matrix, x.end, y.start, this.z()).texture(endU, v).next();
						buffer.vertex(matrix, x.start, y.start, this.z()).texture(u, v).next();
						tessellator.draw();
					}
				}
				case STRETCH -> this.withZ(() -> drawTexture(
					this.matrixes,
					this.absoluteX(),
					this.absoluteY(),
					this.width(),
					this.height(),
					this.u,
					this.v,
					this.regionWidth(),
					this.regionHeight(),
					this.textureWidth(),
					this.textureHeight()
				));
			}

			if (this.isFocused() && this.isActive()) {
				var endX = this.absoluteEndX() - 1;
				var endY = this.absoluteEndY();
				this.drawHorizontalLine(this.absoluteX(), endX, this.absoluteY(), this.z(), -1);
				this.drawVerticalLine(this.absoluteX(), this.absoluteY(), endY, this.z(), -1);
				this.drawVerticalLine(endX, this.absoluteY(), endY, this.z(), -1);
				this.drawHorizontalLine(this.absoluteX(), endX, endY - 1, this.z(), -1);
			}
		}
	}

	protected void resetColor() {
		if (this.isActive()) {
			RenderSystem.setShaderColor(this.r, this.g, this.b, this.a);
		} else {
			var value = 160F / 255;
			RenderSystem.setShaderColor(this.r * value, this.g * value, this.b * value, this.a);
		}
	}

	@Override protected String toLocalString() {
		return super.toLocalString() + " %s[%s + %s, %s + %s]".formatted(this.texture instanceof ResourceTexture r ? r.location : this.texture, this.u, this.regionWidth(), this.v, this.regionHeight());
	}

	int regionWidth() {
		return this.corners[3].x1() - this.corners[0].x0();
	}

	int regionHeight() {
		return this.corners[3].y1() - this.corners[0].y0();
	}
}
