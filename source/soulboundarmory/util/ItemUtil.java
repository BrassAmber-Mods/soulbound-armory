package soulboundarmory.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;
import java.util.stream.Stream;

public class ItemUtil {
	public static Stream<ItemStack> inventory(PlayerEntity player) {
		return combinedInventory(player).flatMap(List::stream);
	}

	public static Stream<DefaultedList<ItemStack>> combinedInventory(PlayerEntity player) {
		var inventory = player.getInventory();
		return Stream.of(inventory.main, inventory.armor, inventory.offHand);
	}

	public static Stream<ItemStack> handStacks(Entity entity) {
		return Util2.stream(entity.getHandItems());
	}
}
