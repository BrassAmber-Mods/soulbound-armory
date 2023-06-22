package soulboundarmory.module.gui;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vector4f;

public record Scissor(int x0, int y0, int x1, int y1) {
	public void apply(MatrixStack matrixes) {
		var vector = new Vector4f(this.x0, this.y0, this.x1, this.y1);
		vector.transform(matrixes.peek().getPositionMatrix());
		DrawableHelper.enableScissor((int) vector.getX(), (int) vector.getY(), (int) vector.getZ(), (int) vector.getW());
	}
}
