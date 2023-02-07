package soulboundarmory.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.component.soulbound.player.MasterComponent;
import soulboundarmory.config.Configuration;
import soulboundarmory.module.gui.widget.ScalableWidget;
import soulboundarmory.module.gui.widget.ScaleMode;
import soulboundarmory.util.ItemUtil;

import static org.lwjgl.opengl.GL11C.*;

public class SelectionEntryWidget extends ScalableWidget<SelectionEntryWidget> {
	private final ItemComponent<?> item;
	private final MasterComponent<?> component;
	private final boolean icon = Configuration.Client.selectionEntryType == Type.ICON;

	public SelectionEntryWidget(ItemComponent<?> item) {
		this.item = item;
		this.component = item.master;

		if (this.icon) {
			this.spikedRectangle(item.isUnlocked() ? 0 : 1)
				.size(64, 64)
				.scaleMode(ScaleMode.STRETCH);
		} else {
			this.button()
				.size(128, 20)
				.text(item.name());
		}
	}

	@Override protected void render() {
		MinecraftClient.getInstance().getFramebuffer().enableStencil();
		glEnable(GL_STENCIL_TEST);
		RenderSystem.stencilMask(~0);
		RenderSystem.clear(GL_STENCIL_BUFFER_BIT, false);
		RenderSystem.stencilFunc(GL_ALWAYS, 1, 0xFF);
		RenderSystem.stencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);

		var x = this.absoluteX();
		var y = this.absoluteY();

		super.render();

		if (this.icon) {
			this.renderItem(x, y);
		}

		RenderSystem.disableDepthTest();
		RenderSystem.stencilFunc(GL_EQUAL, 1, 0xFF);
		RenderSystem.stencilMask(0);
		setPositionColorShader();

		if (!this.isActive()) {
			y = ItemUtil.inventory(player()).anyMatch(this.item::matches) ? y : (int) (y + this.height() * (1 - this.component.cooldown() / 600F));
			fill(this.matrixes, x, y, this.absoluteEndX(), this.absoluteEndY(), this.z(), NativeImage.packColor(95, 0, 0, 0));
		}

		glDisable(GL_STENCIL_TEST);
	}

	private void renderItem(int x, int y) {
		var matrixes = RenderSystem.getModelViewStack();
		var factor = 32F / 13;
		matrixes.push();
		matrixes.scale(factor, factor, 1);
		matrixes.translate(x / factor - x, y / factor - y, 0);
		this.renderGuiItem(this.item.item(), x + 5, y + 5);
		matrixes.pop();
		RenderSystem.applyModelViewMatrix();
	}

	@Override protected void resetColor() {
		this.color3f(1);
	}

	public enum Type {
		ICON,
		TEXT
	}
}
