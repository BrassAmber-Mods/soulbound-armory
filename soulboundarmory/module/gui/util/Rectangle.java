package soulboundarmory.module.gui.util;

public record Rectangle(int x0, int y0, int x1, int y1) {
	public Rectangle() {
		this(0, 0, 0, 0);
	}

	public int width() {
		return this.x1 - this.x0;
	}

	public int height() {
		return this.y1 - this.y0;
	}
}
