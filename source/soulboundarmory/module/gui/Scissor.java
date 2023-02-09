package soulboundarmory.module.gui;

import com.mojang.blaze3d.systems.RenderSystem;

public record Scissor(int x, int y, int width, int height) {
	public void apply() {
		var height = Node.unscale(this.height);
		RenderSystem.enableScissor(Node.unscale(this.x), Node.window.getFramebufferHeight() - Node.unscale(this.y) - height, Node.unscale(this.width), height);
	}
}
