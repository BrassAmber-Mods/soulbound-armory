package soulboundarmory.component.soulbound.item;

import net.minecraft.client.item.TooltipData;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import soulboundarmory.SoulboundArmory;
import soulboundarmory.client.gui.bar.ExperienceBar;
import soulboundarmory.module.component.ItemStackComponent;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.util.Util;

public class ItemMarkerComponent implements ItemStackComponent<ItemMarkerComponent>, TooltipData {
	static final int
		MIN = 2,
		MAX = 15,
		END = MAX + 5;

	public ItemStack stack;
	public ItemComponent<?> item;

	int animationTick = END;
	boolean forward = true;

	public ItemMarkerComponent(ItemStack stack) {
		this.stack = stack;

		if (Util.isClient()) {
			this.initializeItem();
		}
	}

	public ItemComponent<?> item() {
		return this.item == null && Util.isClient() ? this.initializeItem() : this.item;
	}

	public boolean animating() {
		return this.forward ? this.animationTick <= MAX : this.animationTick >= MIN;
	}

	public void animate(boolean forward, ItemStack previous) {
		this.animationTick = forward ? 0 : MAX;
		this.forward = forward;
		if (!forward) this.stack = previous;

		this.upload();
	}

	@OnlyIn(Dist.CLIENT)
	public ExperienceBar tooltip() {
		return new ExperienceBar().item(this.item()).width(Widget.width(this.stack.getName()));
	}

	@Override public void tickStart() {
		if (Util.isClient()) {
			if (this.forward) {
				if (this.animationTick >= END) return;
				++this.animationTick;
			} else if (this.animationTick == MIN) {
				this.stack = this.item.itemStack;
				this.forward = true;
				++this.animationTick;
			} else {
				--this.animationTick;
			}

			this.upload();
		}
	}

	@Override public void serialize(NbtCompound tag) {
		if (this.item() != null) {
			tag.putUuid("player", this.item.player.getUuid());
			tag.putString("item", this.item.type().string());
		}
	}

	@Override public void deserialize(NbtCompound tag) {
		if (!Util.isClient()) {
			this.item = ItemComponentType.get(tag.getString("item")).of(Util.server().getPlayerManager().getPlayer(tag.getUuid("player")));
		}
	}

	@Override public void copy(ItemMarkerComponent copy) {
		ItemStackComponent.super.copy(copy);
		copy.stack = this.stack;
	}

	@OnlyIn(Dist.CLIENT)
	private ItemComponent<?> initializeItem() {
		return this.item = ItemComponent.of(Widget.player(), this.stack).orElse(null);
	}

	@OnlyIn(Dist.CLIENT)
	private void upload() {
		var player = this.item.player;
		var animation = (Sprite.Animation) Widget.itemRenderer.getModel(this.stack, player.world, player, player.getId()).getParticleSprite().getAnimation();

		SoulboundArmory.logger.info("animation {} tick {}", animation, this.animationTick);

		if (animation != null) {
			animation.frameTicks = Integer.MIN_VALUE;
			Widget.bind(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
			animation.upload(Math.max(0, this.animationTick - 3));
		}
	}
}
