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
import soulboundarmory.module.gui.Node;
import soulboundarmory.network.ExtendedPacketBuffer;
import soulboundarmory.network.Packets;
import soulboundarmory.util.ItemUtil;
import soulboundarmory.util.Sided;
import soulboundarmory.util.Util;

import java.util.Map;
import java.util.Optional;

public abstract class MasterComponent<C extends MasterComponent<C>> implements EntityComponent<C>, Sided {
	public final Map<ItemComponentType<? extends ItemComponent<?>>, ItemComponent<?>> items = new Reference2ReferenceLinkedOpenHashMap<>();
	public final PlayerEntity player;

	/**
	 The index of the last open tab in the menu for that it may be restored when the menu is next opened.
	 */
	protected int tab;
	protected int boundSlot;
	protected int cooldown;
	protected ItemComponent<?> item;

	public MasterComponent(PlayerEntity player) {
		this.player = player;
	}

	public static Optional<? extends MasterComponent<?>> of(Entity entity, ItemStack stack) {
		return Components.soulbound(entity).filter(component -> component.matches(stack)).findAny();
	}

	/**
	 @return this component's {@linkplain ComponentRegistry#entity registered} {@linkplain EntityComponentKey key}
	 */
	public abstract EntityComponentKey<? extends MasterComponent<?>> key();

	/**
	 @return whether the given item stack matches any of this component's subcomponents
	 */
	public abstract boolean matches(ItemStack stack);

	/**
	 @return what the name suggests
	 */
	@Override public final boolean isClient() {
		return this.player.world.isClient;
	}

	public int tab() {
		return this.tab;
	}

	public void tab(int index) {
		this.tab = index;

		if (this.isClient()) {
			Packets.serverTab.send(new ExtendedPacketBuffer(this).writeByte(index));
		}
	}

	public int boundSlot() {
		return this.boundSlot;
	}

	public void bindSlot(int boundSlot) {
		this.boundSlot = boundSlot;
	}

	public boolean hasBoundSlot() {
		return this.boundSlot != -1;
	}

	public void unbindSlot() {
		this.boundSlot = -1;
	}

	/**
	 @return the item stack in the bound slot
	 @throws ArrayIndexOutOfBoundsException if no slot is bound
	 */
	public final ItemStack stackInBoundSlot() {
		return this.player.getInventory().getStack(this.boundSlot);
	}

	/**
	 @return the active soulbound item component
	 */
	public Optional<? extends ItemComponent<?>> item() {
		return Optional.ofNullable(this.item).filter(ItemComponent::isUnlocked);
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

	/**
	 Set the currently active soulbound item, ensure that it is unlocked, update bound slot and synchronize.

	 @param item the item's component
	 */
	public void set(ItemComponent<?> item, int slot) {
		if (item.isEnabled() && this.item().filter(i -> i == item).isEmpty()) {
			this.cooldown = 600;
			this.item = item;

			if (this.hasBoundSlot()) {
				this.bindSlot(slot);
			}

			if (item.isUnlocked()) {
				this.refresh();
			} else {
				item.unlock();
			}

			item.synchronize();
		}
	}

	/**
	 Returns the remaining item selection cooldown period which is always 0 for players in creative mode.

	 @return the remaining cooldown period
	 */
	public int cooldown() {
		return this.player.isCreative() ? 0 : this.cooldown;
	}

	/**
	 Sets an item selection cooldown period.

	 @param cooldown a new cooldown period
	 */
	public void cooldown(int cooldown) {
		this.cooldown = cooldown;
	}

	/**
	 Returns whether this component has cooled down.
	 If true, then the player is not prevented from selecting a different item.

	 @return whether this component has cooled down
	 */
	public boolean cooledDown() {
		return this.cooldown() <= 0;
	}

	/**
	 @return the selection tab for this component
	 */
	@OnlyIn(Dist.CLIENT)
	public Tab selectionTab() {
		return new SelectionTab();
	}

	/**
	 @return the item component that matches `stack`
	 */
	public Optional<ItemComponent<?>> component(ItemStack stack) {
		return this.items.values().stream().filter(component -> component.matches(stack)).findAny();
	}

	/**
	 @return the item component corresponding to the first held item stack that matches this component
	 */
	public Optional<ItemComponent<?>> heldItemComponent() {
		return ItemUtil.handStacks(this.player).flatMap(stack -> this.component(stack).stream()).findFirst();
	}

	/**
	 Open a GUI for this component if {@code stack} is {@linkplain ItemComponent#canConsume consumable} or this component is {@linkplain #matches applicable} to it.

	 @param stack the item stack to test
	 @param slot the index of the inventory slot wherein the stack resides
	 @return whether a GUI was opened
	 */
	public boolean tryOpenGUI(ItemStack stack, int slot) {
		if (this.matches(stack) || this.items.values().stream().anyMatch(item -> item.canConsume(stack))) {
			new SoulboundScreen(this, slot).open();
			return true;
		}

		return false;
	}

	/**
	 Reinitialize the current {@linkplain SoulboundScreen menu} if open.
	 */
	public void refresh() {
		if (this.isClient()) {
			if (Node.cellScreen() instanceof SoulboundScreen screen) {
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
		Packets.clientSync.sendIfServer(this.player, new ExtendedPacketBuffer(this).writeNbt(this.serialize()));
	}

	@Override public void tickStart() {
		this.updateInventory();
		this.items.values().forEach(ItemComponent::tick);

		if (!this.cooledDown()) {
			this.cooldown--;
		}
	}

	/**
	 Add an item component to this component.

	 @param item the item component
	 */
	protected void store(ItemComponent<?>... items) {
		for (var item : items) {
			this.items.put(item.type(), item);
		}
	}

	/**
	 Update item components' item stacks and synchronize them.
	 */
	@Override public void spawn() {
		this.items.values().forEach(ItemComponent::updateItemStack);
		this.synchronize();
	}

	@Override public void copy(C copy) {
		EntityComponent.super.copy(copy);

		if (!this.player.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
			this.item().ifPresent(component -> {
				if (component.level() >= Configuration.preservationLevel) {
					this.player.getInventory().insertStack(component.stack());
				}
			});
		}
	}

	@Override public void serialize(NbtCompound tag) {
		this.item().ifPresent(itemComponent -> tag.putString("item", itemComponent.type().id().toString()));

		for (var storage : this.items.values()) {
			tag.put(storage.type().id().toString(), storage.serialize());
		}

		tag.putInt("tab", this.tab);
		tag.putInt("slot", this.boundSlot);
		tag.putInt("cooldown", this.cooldown);
	}

	@Override public void deserialize(NbtCompound tag) {
		var type = ItemComponentType.get(tag.getString("item"));

		if (type != null) {
			this.item = this.item(type);
		}

		for (var item : this.items.values()) {
			Util.ifPresent(tag, item.type().string(), item::deserialize);
		}

		this.tab = tag.getInt("tab");
		this.bindSlot(tag.getInt("slot"));
		this.cooldown = tag.getInt("cooldown");
	}

	/**
	 Scan the inventory and clean it up.
	 <p>
	 Let {@code bs} = this.boundSlot if the bound slot contains an item matching the active subcomponent; else -1.
	 <p>
	 - Remove {@link ItemComponent#isEnabled disabled} subcomponent items.
	 <br>
	 - If a slot is bound and does not contain an item matching the active subcomponent and a different such slot is encountered, then bind it the first time.
	 <br>
	 - If the player is not in creative mode,
	 then replace subcomponent items stacks that do not match the active component or are not in {@code bs} by their {@link ItemComponent#consumableItem}s.
	 <br>
	 - If a matching item stack is encountered and it does not equal {@link ItemComponent#stack}, then replace it by a copy thereof.
	 */
	private void updateInventory() {
		var active = this.item();
		var bs = this.hasBoundSlot() && active.filter(item -> item.matches(this.stackInBoundSlot())).isPresent() ? this.boundSlot : -1;
		var inventory = this.player.getInventory();

		for (var slot = 0; slot < inventory.size(); ++slot) {
			var stack = inventory.getStack(slot);

			if (this.matches(stack)) {
				var item = this.component(stack).get();

				if (item.isEnabled() && (this.player.isCreative() || item == active.orElse(null) && (slot == bs || bs == -1))) {
					if (item == active.orElse(null)) {
						if (bs == -1) {
							bs = slot;

							if (this.hasBoundSlot()) {
								this.bindSlot(bs);
							}
						}

						if (slot == bs) {
							inventory.setStack(bs, item.stack());
						}
					} else if (stack == this.player.getMainHandStack()) {
						this.set(item, slot);
					}
				} else {
					inventory.setStack(slot, item.consumableItem().getDefaultStack());
				}
			}
		}
	}
}
