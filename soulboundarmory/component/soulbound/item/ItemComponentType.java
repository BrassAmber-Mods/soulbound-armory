package soulboundarmory.component.soulbound.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraftforge.registries.IForgeRegistry;
import soulboundarmory.text.Translations;
import soulboundarmory.component.Components;
import soulboundarmory.component.soulbound.item.armor.*;
import soulboundarmory.component.soulbound.item.tool.*;
import soulboundarmory.component.soulbound.item.weapon.*;
import soulboundarmory.component.soulbound.player.MasterComponent;
import soulboundarmory.item.SoulboundItems;
import soulboundarmory.module.component.EntityComponentKey;
import soulboundarmory.module.transform.Register;
import soulboundarmory.module.transform.RegisterAll;
import soulboundarmory.module.transform.Registry;
import soulboundarmory.registry.Identifiable;

import java.util.Optional;

@RegisterAll(type = ItemComponentType.class, registry = "item_component")
public final class ItemComponentType<C extends ItemComponent<C>> extends Identifiable {
	@Register("dagger") public static final ItemComponentType<DaggerComponent> dagger = weapon(SoulboundItems.dagger, Items.WOODEN_SWORD);
	@Register("sword") public static final ItemComponentType<SwordComponent> sword = weapon(SoulboundItems.sword, Items.WOODEN_SWORD);
	@Register("bigsword") public static final ItemComponentType<BigswordComponent> bigsword = weapon(SoulboundItems.bigsword, Items.WOODEN_SWORD);
	@Register("greatsword") public static final ItemComponentType<GreatswordComponent> greatsword = weapon(SoulboundItems.greatsword, Items.WOODEN_SWORD);
	@Register("trident") public static final ItemComponentType<TridentComponent> trident = weapon(SoulboundItems.trident, Items.TRIDENT);

	@Register("pickaxe") public static final ItemComponentType<PickaxeComponent> pickaxe = tool(SoulboundItems.pickaxe, Items.WOODEN_PICKAXE);
	@Register("axe") public static final ItemComponentType<AxeComponent> axe = tool(SoulboundItems.axe, Items.WOODEN_PICKAXE);
	@Register("shovel") public static final ItemComponentType<ShovelComponent> shovel = tool(SoulboundItems.shovel, Items.WOODEN_PICKAXE);
	@Register("hoe") public static final ItemComponentType<HoeComponent> hoe = tool(SoulboundItems.hoe, Items.WOODEN_PICKAXE);

	@Register("helmet") public static final ItemComponentType<HelmetComponent> helmet = armor(SoulboundItems.helmet, Items.LEATHER_HELMET);
	@Register("chestplate") public static final ItemComponentType<ChestplateComponent> chestplate = armor(SoulboundItems.chestplate, Items.LEATHER_HELMET);
	@Register("leggings") public static final ItemComponentType<LeggingsComponent> leggings = armor(SoulboundItems.leggings, Items.LEATHER_HELMET);
	@Register("boots") public static final ItemComponentType<BootsComponent> boots = armor(SoulboundItems.boots, Items.LEATHER_HELMET);

	public final EntityComponentKey<? extends MasterComponent<?>> parentKey;
	public final Item item;
	public final Item consumableItem;

	public ItemComponentType(EntityComponentKey<? extends MasterComponent<?>> key, Item item, Item consumableItem) {
		this.parentKey = key;
		this.item = item;
		this.consumableItem = consumableItem;
	}

	@Registry("item_component") public static native <C extends ItemComponent<C>> IForgeRegistry<ItemComponentType<C>> registry();

	static <T extends WeaponComponent<T>> ItemComponentType<T> weapon(Item item, Item consumableItem) {
		return new ItemComponentType<>(Components.weapon, item, consumableItem);
	}

	static <T extends ToolComponent<T>> ItemComponentType<T> tool(Item item, Item consumableItem) {
		return new ItemComponentType<>(Components.tool, item, consumableItem);
	}

	static <T extends ArmorComponent<T>> ItemComponentType<T> armor(Item item, Item consumableItem) {
		return new ItemComponentType<>(Components.armor, item, consumableItem);
	}

	public Text name() {
		return Translations.gui(ItemComponentType.<C>registry().getKey(this).getPath()).text();
	}

	public static ItemComponentType<?> get(Identifier id) {
		return registry().getValue(id);
	}

	public static ItemComponentType<?> get(String name) {
		return get(new Identifier(name));
	}

	public C of(Entity entity) {
		return this.parentKey.optional(entity).map(component -> component.item(this)).orElse(null);
	}

	public Optional<C> nullable(Entity entity) {
		return Optional.ofNullable(this.of(entity));
	}

	@Override public String toString() {
		return "item component type " + this.id();
	}
}
