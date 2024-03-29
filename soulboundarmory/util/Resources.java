package soulboundarmory.util;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import soulboundarmory.module.gui.widget.Widget;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Optional;

public class Resources {
	public static BufferedImage readTexture(Identifier identifier) {
		return resource(identifier).map(resource -> ImageIO.read(resource.getInputStream())).orElse(null);
	}

	public static Optional<Resource> resource(Identifier identifier) {
		return Widget.resourceManager.getResource(identifier);
	}

	public static int[][][] pixels(BufferedImage image, int u, int v, int width, int height) {
		var pixels = new int[image.getHeight()][image.getWidth()][4];
		var raster = image.getData();

		for (var y = 0; y < height; y++) {
			for (var x = 0; x < width; x++) {
				pixels[y][x] = raster.getPixel(u + x, v + y, (int[]) null);
			}
		}

		return pixels;
	}
}
