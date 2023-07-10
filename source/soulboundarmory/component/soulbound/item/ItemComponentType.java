package soulboundarmory.component.soulbound.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraftforge.registries.IForgeRegistry;
import soulboundarmory.client.i18n.Translations;
import soulboundarmory.component.Components;
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
	@Register("dagger") public static final ItemComponentType<DaggerComponent> dagger = weapon(SoulboundItems.dagger, Items.WOODEN_SWORD, Translations.guiDagger);
	@Register("sword") public static final ItemComponentType<SwordComponent> sword = weapon(SoulboundItems.sword, Items.WOODEN_SWORD, Translations.guiSword);
	@Register("bigsword") public static final ItemComponentType<BigswordComponent> bigsword = weapon(SoulboundItems.bigsword, Items.WOODEN_SWORD, Translations.guiBigsword);
	@Register("greatsword") public static final ItemComponentType<GreatswordComponent> greatsword = weapon(SoulboundItems.greatsword, Items.WOODEN_SWORD, Translations.guiGreatsword);
	@Register("trident") public static final ItemComponentType<TridentComponent> trident = weapon(SoulboundItems.trident, Items.TRIDENT, Translations.guiTrident);

	@Register("pickaxe") public static final ItemComponentType<PickaxeComponent> pickaxe = tool(SoulboundItems.pickaxe, Items.WOODEN_PICKAXE, Translations.guiPickaxe);
	@Register("axe") public static final ItemComponentType<AxeComponent> axe = tool(SoulboundItems.axe, Items.WOODEN_PICKAXE, Translations.guiAxe);
	@Register("shovel") public static final ItemComponentType<ShovelComponent> shovel = tool(SoulboundItems.shovel, Items.WOODEN_PICKAXE, Translations.guiShovel);
	@Register("hoe") public static final ItemComponentType<HoeComponent> hoe = tool(SoulboundItems.hoe, Items.WOODEN_PICKAXE, Translations.guiHoe);

	public final EntityComponentKey<? extends MasterComponent<?>> parentKey;
	public final Item item;
	public final Item consumableItem;
	public final Text name;

	public ItemComponentType(EntityComponentKey<? extends MasterComponent<?>> key, Item item, Item consumableItem, Text name) {
		this.parentKey = key;
		this.item = item;
		this.consumableItem = consumableItem;
		this.name = name;
	}

	@Registry("item_component") public static native <C extends ItemComponent<C>> IForgeRegistry<ItemComponentType<C>> registry();

	public static <T extends WeaponComponent<T>> ItemComponentType<T> weapon(Item item, Item consumableItem, Text name) {
		return new ItemComponentType<>(Components.weapon, item, consumableItem, name);
	}

	public static <T extends ToolComponent<T>> ItemComponentType<T> tool(Item item, Item consumableItem, Text name) {
		return new ItemComponentType<>(Components.tool, item, consumableItem, name);
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
