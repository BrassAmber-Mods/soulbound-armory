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

	public final Rectangle[] middles = Util2.fill(new Rectangle[5], Rectangle::new);
	public final Rectangle[] corners = Util2.fill(new Rectangle[4], Rectangle::new);
	public final Rectangle border = new Rectangle();
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
		var topLeft = this.corners[0];
		var topRight = this.corners[1];
		var bottomLeft = this.corners[2];
		var bottomRight = this.corners[3];
		bottomLeft.end.x = topLeft.end.x = u0;
		bottomRight.start.x = topRight.start.x = u1;
		bottomRight.end.x = topRight.end.x = u2;
		topRight.end.y = topLeft.end.y = v0;
		bottomRight.start.y = bottomLeft.start.y = v1;
		bottomRight.end.y = bottomLeft.end.y = v2;

		var top = this.middles[0];
		var left = this.middles[1];
		var center = this.middles[2];
		var right = this.middles[3];
		var bottom = this.middles[4];
		bottom.start.x = center.start.x = left.end.x = top.start.x = u0;
		bottom.end.x = right.start.x = center.end.x = top.end.x = u1;
		right.end.x = u2;
		right.start.y = center.start.y = left.start.y = top.end.y = v0;
		bottom.start.y = right.end.y = center.end.y = left.end.y = v1;
		bottom.end.y = v2;

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
		return this.resolve(this.width, this.textureWidth);
	}

	@Override public int height() {
		return this.resolve(this.height, this.textureHeight);
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
		pushScissor(this.absoluteX(), this.absoluteY(), this.viewWidth(), this.viewHeight());
		RenderSystem.enableBlend();
		shaderTexture(this.texture);
		this.resetColor();

		switch (this.scaleMode) {
			case SLICE -> {
				this.renderCorners();
				this.renderMiddles();
			}
			case STRETCH -> {
				this.matrixes.push();
				this.matrixes.translate(0, 0, this.z());
				drawTexture(
					this.matrixes,
					this.absoluteX(),
					this.absoluteY(),
					this.width(),
					this.height(),
					this.u,
					this.v,
					this.corners[3].end.x - this.corners[0].start.x,
					this.corners[3].end.y - this.corners[0].start.y,
					this.textureWidth(),
					this.textureHeight()
				);
				this.matrixes.pop();
			}
		}

		if (this.isFocused() && this.isActive()) {
			this.drawBorder();
		}

		popScissor();
	}

	protected void renderCorners() {
		for (var index = 0; index < this.corners.length; ++index) {
			var corner = this.corners[index];
			var width = corner.width();
			var height = corner.height();

			drawTexture(
				this.matrixes,
				this.absoluteX() + index % 2 * (this.width() - corner.width()),
				this.absoluteY() + index / 2 * (this.height() - corner.height()),
				this.z(),
				this.u + corner.start.x,
				this.v + corner.start.y,
				width,
				height,
				this.textureWidth(),
				this.textureHeight()
			);
		}
	}

	protected void renderMiddles() {
		var tessellator = Tessellator.getInstance();
		var buffer = tessellator.getBuffer();
		var matrix = this.matrixes.peek().getPositionMatrix();

		for (var index = 0; index < this.middles.length; ++index) {
			var middle = this.middles[index];
			var x = switch (index) {
				case 1 -> this.absoluteX();
				case 3 -> this.absoluteEndX() - middle.width();
				default -> this.absoluteX() + this.middles[1].width();
			};
			var y = switch (index) {
				case 0 -> this.absoluteY();
				case 4 -> this.absoluteEndY() - middle.height();
				default -> this.absoluteY() + this.middles[0].height();
			};
			var endX = switch (index) {
				case 1 -> this.absoluteX() + middle.width();
				case 3 -> this.absoluteEndX();
				default -> this.absoluteEndX() - this.middles[3].width();
			};
			var endY = switch (index) {
				case 0 -> this.absoluteY() + middle.height();
				case 4 -> this.absoluteEndY();
				default -> this.absoluteEndY() - this.middles[4].height();
			};

			var textureWidth = (float) this.textureWidth();
			var textureHeight = (float) this.textureHeight();
			var u = (this.u + middle.start.x) / textureWidth;
			var v = (this.v + middle.start.y) / textureHeight;
			var endU = u + Math.min(endX - x, middle.width()) / textureWidth;
			var endV = v + Math.min(endY - y, middle.height()) / textureHeight;
			buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
			buffer.vertex(matrix, x, endY, this.z()).texture(u, endV).next();
			buffer.vertex(matrix, endX, endY, this.z()).texture(endU, endV).next();
			buffer.vertex(matrix, endX, y, this.z()).texture(endU, v).next();
			buffer.vertex(matrix, x, y, this.z()).texture(u, v).next();
			tessellator.draw();
		}
	}

	protected void drawBorder() {
		var endX = this.absoluteEndX() - 1;
		var endY = this.absoluteEndY();
		drawHorizontalLine(this.matrixes, this.absoluteX(), endX, this.absoluteY(), this.z(), -1);
		drawVerticalLine(this.matrixes, this.absoluteX(), this.absoluteY(), endY, this.z(), -1);
		drawVerticalLine(this.matrixes, endX, this.absoluteY(), endY, this.z(), -1);
		drawHorizontalLine(this.matrixes, this.absoluteX(), endX, endY - 1, this.z(), -1);
	}

	protected void resetColor() {
		if (this.isActive()) {
			RenderSystem.setShaderColor(this.r, this.g, this.b, this.a);
		} else {
			var value = 160F / 255;
			RenderSystem.setShaderColor(this.r * value, this.g * value, this.b * value, this.a);
		}
	}
}
