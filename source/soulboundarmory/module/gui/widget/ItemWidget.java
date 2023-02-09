package soulboundarmory.module.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemWidget extends Widget<ItemWidget> {
	public ItemStack item;

	public ItemWidget item(Item item) {
		return this.item(item.getDefaultStack());
	}

	public ItemWidget item(ItemStack item) {
		this.item = item;
		return this;
	}

	public ItemWidget tooltip() {
		return this.tooltip(new GraphicWidget(gw -> screen().renderTooltip(this.matrixes, this.item, this.mouseFocused ? (int) mouseX() : this.absoluteX() - 8, this.mouseFocused ? (int) mouseY() : this.absoluteY())));
	}

	@Override protected void render() {
		var matrixes = RenderSystem.getModelViewStack();
		matrixes.push();
		matrixes.translate(this.absoluteX(), this.absoluteY(), 0);
		matrixes.scale(this.width() / 16F, this.height() / 16F, 1);
		this.renderGuiItem(this.item, 0, 0);
		matrixes.pop();
		RenderSystem.applyModelViewMatrix();
	}
}
