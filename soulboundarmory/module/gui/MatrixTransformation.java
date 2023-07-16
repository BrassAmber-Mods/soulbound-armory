package soulboundarmory.module.gui;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

public record MatrixTransformation(MatrixStack matrixes) implements AutoCloseable {
	public MatrixTransformation {
		matrixes.push();
	}

	@Override public void close() {
		this.matrixes.pop();
	}

	public MatrixTransformation translate(double x, double y, double z) {
		this.matrixes.translate(x, y, z);
		return this;
	}

	public MatrixTransformation scale(float x, float y, float z) {
		this.matrixes.scale(x, y, z);
		return this;
	}

	public MatrixTransformation scale(float factor) {
		return this.scale(factor, factor, factor);
	}

	public MatrixTransformation multiply(Quaternion quaternion) {
		this.matrixes.multiply(quaternion);
		return this;
	}

	public MatrixTransformation rotate(Vec3f axis, float degrees) {
		return this.multiply(axis.getDegreesQuaternion(degrees));
	}
}
