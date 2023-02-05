package soulboundarmory.module.text.mixin;

import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import soulboundarmory.module.text.ExtendedFormatting;
import soulboundarmory.module.text.access.ExtendedStyle;
import soulboundarmory.module.text.access.FontRenderer$DrawerAccess;

import java.util.List;

@SuppressWarnings("public-target")
@Mixin(targets = "net.minecraft.client.font.TextRenderer$Drawer")
abstract class FontRenderer$DrawerMixin implements FontRenderer$DrawerAccess {
	@Accessor("light")
	@Override public abstract int light();

	@Accessor("brightnessMultiplier")
	@Override public abstract float brightnessMultiplier();

	@Accessor("red")
	@Override public abstract float r();

	@Accessor("green")
	@Override public abstract float g();

	@Accessor("blue")
	@Override public abstract float b();

	@Accessor("alpha")
	@Override public abstract float a();

	@Accessor("x")
	@Override public abstract float x();

	@Accessor("y")
	@Override public abstract float y();

	@Accessor("shadow")
	@Override public abstract boolean shadow();

	@Accessor("matrix")
	@Override public abstract Matrix4f pose();

	@Accessor("rectangles")
	@Override public abstract List<GlyphRenderer.Rectangle> rectangles();

	@Accessor("vertexConsumers")
	@Override public abstract VertexConsumerProvider vertexConsumers();

	@Invoker("drawLayer")
	@Override public abstract float invokeFinish(int underlineColor, float x);

	@Invoker("addRectangle")
	@Override public abstract void invokeAddRectangle(GlyphRenderer.Rectangle rectangle);

	@Inject(method = "accept",
	        at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/font/TextRenderer$Drawer;x:F"),
	        locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	public void formatCustom(
		int charIndex,
		Style style,
		int character,
		CallbackInfoReturnable<Boolean> info,
		FontStorage font,
		Glyph glyph,
		GlyphRenderer glyphRenderer,
		boolean isBold,
		float alpha,
		float red,
		float green,
		float blue,
		TextColor color,
		float advance
	) {
		for (var formatting : ((ExtendedStyle) style).formattings()) {
			if ((Object) formatting instanceof ExtendedFormatting extendedFormatting) {
				var formatter = extendedFormatting.formatter();

				if (formatter != null) {
					formatter.format(this, style, charIndex, character, font, glyph, glyphRenderer, color, red, green, blue, advance);
				}
			}
		}
	}
}
