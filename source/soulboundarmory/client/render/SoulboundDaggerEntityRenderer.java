package soulboundarmory.client.render;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import soulboundarmory.entity.SoulboundDaggerEntity;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.util.Util;

public class SoulboundDaggerEntityRenderer extends EntityRenderer<SoulboundDaggerEntity> {
	private static final Identifier id = Util.id("textures/item/dagger/0.png");

	public SoulboundDaggerEntityRenderer(EntityRendererFactory.Context context) {
		super(context);
	}

	@Override public Identifier getTexture(SoulboundDaggerEntity entity) {
		return entity.component().map(component -> Widget.itemRenderer.getModel(component.stack(), entity.world, component.player, component.player.getId()).getParticleSprite().getId()).orElse(id);
	}

	@Override public void render(SoulboundDaggerEntity dagger, float yaw, float tickDelta, MatrixStack matrixes, VertexConsumerProvider vertexConsumers, int light) {
		try (var ms = Widget.push(matrixes)
			.rotate(Vec3f.POSITIVE_Y, MathHelper.lerp(tickDelta, dagger.prevYaw, dagger.getYaw()) + 90)
			.rotate(Vec3f.POSITIVE_Z, 45 - MathHelper.lerp(tickDelta, dagger.prevPitch, dagger.getPitch()))
		) {
			var shake = dagger.shake - tickDelta;

			if (shake > 0) {
				ms.rotate(Vec3f.POSITIVE_Z, -MathHelper.sin(shake * 3) * shake);
			}

			ms.scale(0.75F);
			Widget.itemRenderer.renderItem(dagger.asItemStack(), ModelTransformation.Mode.FIXED, light, 0, matrixes, vertexConsumers, dagger.age);
		}

		super.render(dagger, yaw, tickDelta, matrixes, vertexConsumers, light);
	}
}
