package soulboundarmory.module.gui;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vector4f;

public record Scissor(int x, int y, int width, int height) {
	public void apply(MatrixStack matrixes) {
		var vector = new Vector4f(this.x, this.y, this.x + this.width, this.y + this.height);
		vector.transform(matrixes.peek().getPositionMatrix());
		DrawableHelper.enableScissor((int) vector.getX(), (int) vector.getY(), (int) vector.getZ(), (int) vector.getW());
	}
}
