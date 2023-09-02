package soulboundarmory.component.soulbound.player;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import soulboundarmory.client.gui.screen.SelectionTab;
import soulboundarmory.client.gui.screen.SoulboundScreen;
import soulboundarmory.client.gui.screen.Tab;
import soulboundarmory.component.Components;
import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.config.Configuration;
import soulboundarmory.module.component.ComponentRegistry;
import soulboundarmory.module.component.EntityComponent;
import soulboundarmory.module.component.EntityComponentKey;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.network.ExtendedPacketBuffer;
import soulboundarmory.network.Packets;
import soulboundarmory.util.ItemUtil;
import soulboundarmory.util.Sided;
import soulboundarmory.util.Util;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class MasterComponent<C extends MasterComponent<C>> implements EntityComponent<C>, Sided {
	public final Map<ItemComponentType<? extends ItemComponent<?>>, ItemComponent<?>> items = new Reference2ReferenceLinkedOpenHashMap<>();
	public final PlayerEntity player;

	public int tab;
	protected int cooldown;

	public MasterComponent(PlayerEntity player) {
		this.player = player;
	}

	public static Optional<? extends MasterComponent<?>> of(Entity entity, ItemStack stack) {
		return Components.soulbound(entity).filter(component -> component.matches(stack)).findAny();
	}

	/** @return this component's {@linkplain ComponentRegistry#entity registered} {@linkplain EntityComponentKey key} */
	public abstract EntityComponentKey<? extends MasterComponent<?>> key();

	/** @return whether the given item stack matches any of this component's subcomponents */
	public abstract boolean matches(ItemStack stack);

	@Override public final boolean isClient() {
		return this.player.world.isClient;
	}

	public void tab(int index) {
		this.tab = index;
		Packets.serverTab.sendIfClient(() -> new ExtendedPacketBuffer(this).writeByte(index));
	}

	public Stream<ItemComponent<?>> items() {
		return this.items.values().stream();
	}

	public Stream<ItemComponent<?>> active() {
		return this.items().filter(item -> item.active);
	}

	/**
	 Find this component's item component of the given type.

	 @param type the item component type
	 @param <S> the class of the item component
	 @return the item component if it exists or null
	 */
	public <S extends ItemComponent<S>> S item(ItemComponentType<S> type) {
		return (S) this.items.get(type);
	}

	public void activate(ItemComponent<?> item, int slot) {
		if (item.isEnabled() && !item.active) {
			if (item.level() < 100) {
				this.active().filter(subcomponent -> subcomponent.level() < 100).forEach(active -> active.active = false);
			}

			item.active = true;
			this.cooldown = 600;

			if (item.boundSlot != -1) {
				item.boundSlot = slot;
			}

			if (item.unlocked) {
				this.refresh();
			} else {
				item.unlock(slot);
			}

			item.synchronize();
		}
	}

	/** @return the remaining item selection cooldown period which is always 0 for players in creative mode */
	public int cooldown() {
		return this.player.isCreative() ? 0 : this.cooldown;
	}

	/** Sets an item selection cooldown period. */
	public void cooldown(int cooldown) {
		this.cooldown = cooldown;
	}

	@OnlyIn(Dist.CLIENT)
	public Tab selectionTab() {
		return new SelectionTab();
	}

	public Optional<ItemComponent<?>> item(ItemStack stack) {
		return this.items().filter(item -> item.matches(stack)).findAny();
	}

	/** @return the item component corresponding to the first held item stack that matches this component */
	public Optional<ItemComponent<?>> heldItemComponent() {
		return ItemUtil.handStacks(this.player).flatMap(stack -> this.item(stack).stream()).findFirst();
	}

	/**
	 Open a GUI for this component if {@code stack} is {@linkplain ItemComponent#canConsume consumable} or this component is {@linkplain #matches applicable} to it.

	 @param stack the item stack to test
	 @param slot the index of the inventory slot wherein the stack resides
	 @return whether a GUI was opened
	 */
	public boolean tryOpenGUI(ItemStack stack, int slot) {
		if (this.matches(stack) || this.items().anyMatch(item -> item.canConsume(stack))) {
			new SoulboundScreen(this, slot).open();
			return true;
		}

		return false;
	}

	/** Reinitialize the current {@linkplain SoulboundScreen menu} if open. */
	public void refresh() {
		if (this.isClient()) {
			if (Widget.cellScreen() instanceof SoulboundScreen screen) {
				screen.refresh();
			}
		} else {
			Packets.clientRefresh.send(this.player, new ExtendedPacketBuffer(this));
		}
	}

	/**
	 Ensure that the client's component is up to date with the server.

	 @see #serialize(NbtCompound)
	 @see #deserialize(NbtCompound)
	 */
	public void synchronize() {
		Packets.clientSync.sendIfServer(this.player, () -> new ExtendedPacketBuffer(this).writeNbt(this.serialize()));
	}

	@Override public void tickStart() {
		this.updateInventory();
		this.items.values().forEach(ItemComponent::tick);

		if (this.cooldown() > 0) {
			this.cooldown--;
		}
	}

	protected void store(ItemComponent<?>... items) {
		for (var item : items) {
			this.items.put(item.type(), item);
		}
	}

	/** Update item components' item stacks and synchronize them. */
	@Override public void spawn() {
		this.items.values().forEach(ItemComponent::updateItemStack);
		this.synchronize();
	}

	@Override public void copy(C copy) {
		EntityComponent.super.copy(copy);

		if (!this.player.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
			this.active().filter(item -> item.level() >= Configuration.preservationLevel).forEach(item -> {
				copy.player.giveItemStack(item.stack());
			});
		}
	}

	@Override public void serialize(NbtCompound tag) {
		for (var storage : this.items.values()) {
			tag.put(storage.type().id().toString(), storage.serialize());
		}

		tag.putInt("tab", this.tab);
		tag.putInt("cooldown", this.cooldown);
	}

	@Override public void deserialize(NbtCompound tag) {
		for (var item : this.items.values()) {
			Util.ifPresent(tag, item.type().string(), item::deserialize);
		}

		this.tab = tag.getInt("tab");
		this.cooldown = tag.getInt("cooldown");
	}

	void updateInventory() {
		var inventory = this.player.getInventory();

		for (var slot = 0; slot < inventory.size(); ++slot) {
			var stack = inventory.getStack(slot);

			if (this.matches(stack)) {
				var item = this.item(stack).get();
				var bs = item.boundSlot != -1 && item.matches(item.stackInBoundSlot()) ? item.boundSlot : -1;

				if (item.isEnabled() && (this.player.isCreative() || item.active && (slot == bs || bs == -1))) {
					if (item.active) {
						if (bs == -1) bs = item.boundSlot == -1 ? slot : (item.boundSlot = slot);
						if (slot == bs) inventory.setStack(bs, item.stack());
					} else if (stack == this.player.getMainHandStack()) {
						this.activate(item, slot);
					}
				} else {
					inventory.setStack(slot, item.consumableItem().getDefaultStack());
				}
			}
		}
	}
}
