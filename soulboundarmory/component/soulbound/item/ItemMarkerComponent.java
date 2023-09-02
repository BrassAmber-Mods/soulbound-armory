package soulboundarmory.component.soulbound.item;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import soulboundarmory.client.gui.bar.ExperienceBar;
import soulboundarmory.module.component.ItemStackComponent;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.util.Util;

import java.util.Optional;

public class ItemMarkerComponent implements ItemStackComponent<ItemMarkerComponent>, TooltipData {
	public final ItemStack stack;

	private ItemComponent<?> item;
	private int animationTick = 40;

	public ItemMarkerComponent(ItemStack stack) {
		this.stack = stack;

		if (Util.isClient()) {
			this.initializeItem();
		}
	}

	public Optional<ItemComponent<?>> optionalItem() {
		return Optional.ofNullable(this.item());
	}

	public ItemComponent<?> item() {
		if (this.item == null && Util.isClient()) {
			return this.initializeItem();
		}

		return this.item;
	}

	public void item(ItemComponent<?> item) {
		this.item = item;
	}

	public boolean animating() {
		return this.animationTick < 30;
	}

	public void unlock() {
		this.animationTick = 0;
	}

	@OnlyIn(Dist.CLIENT)
	public ExperienceBar tooltip() {
		return new ExperienceBar().item(this.item()).width(Widget.width(this.stack.getName()));
	}

	@Override public void tickStart() {
		if (Util.isClient() && this.animating()) this.animationTick++;
	}

	@Override public void serialize(NbtCompound tag) {
		this.optionalItem().ifPresent(item -> {
			tag.putUuid("player", item.player.getUuid());
			tag.putString("item", item.type().string());
		});
	}

	@Override public void deserialize(NbtCompound tag) {
		if (!Util.isClient()) {
			this.item = ItemComponentType.get(tag.getString("item")).of(Util.server().getPlayerManager().getPlayer(tag.getUuid("player")));
		}
	}

	@OnlyIn(Dist.CLIENT)
	private ItemComponent<?> initializeItem() {
		return this.item = ItemComponent.of(Widget.player(), this.stack).orElse(null);
	}
}
