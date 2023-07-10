package soulboundarmory.item;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.auoeke.reflect.Invoker;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;
import soulboundarmory.SoulboundArmory;
import soulboundarmory.module.transform.Register;
import soulboundarmory.module.transform.RegisterAll;
import soulboundarmory.util.Util2;

import java.util.List;
import java.util.Map;

import static net.auoeke.dycon.Dycon.ldc;

@RegisterAll(type = Item.class, registry = "item")
public class SoulboundItems {
	public static final ToolMaterial baseMaterial = material(ToolMaterials.WOOD);

	@Register("dagger") public static final SoulboundDaggerItem dagger = new SoulboundDaggerItem();
	@Register("sword") public static final SoulboundSwordItem sword = new SoulboundSwordItem();
	@Register("greatsword") public static final SoulboundGreatswordItem greatsword = new SoulboundGreatswordItem();
	@Register("bigsword") public static final SoulboundBigswordItem bigsword = new SoulboundBigswordItem();
	@Register("trident") public static final SoulboundTridentItem trident = new SoulboundTridentItem();

	@Register("pickaxe") public static final SoulboundPickaxeItem pickaxe = new SoulboundPickaxeItem();
	@Register("axe") public static final SoulboundAxeItem axe = new SoulboundAxeItem();
	@Register("shovel") public static final SoulboundShovelItem shovel = new SoulboundShovelItem();
	@Register("hoe") public static final SoulboundHoeItem hoe = new SoulboundHoeItem();

	public synchronized static ToolMaterial material(ToolMaterial previous) {
		var materials = ldc(() -> new Reference2ReferenceOpenHashMap<ToolMaterial, Map<TagKey<Block>, ToolMaterial>>());
		return materials.computeIfAbsent(previous, p -> new Reference2ReferenceOpenHashMap<>()).computeIfAbsent(previous.getTag(), tag -> {
			var lower = TierSortingRegistry.getTiersLowerThan(previous);
			var material = TierSortingRegistry.registerTier(
				new ForgeTier(previous.getMiningLevel(), 0, 1.5F, 0, 0, tag, () -> null),
				new Identifier(SoulboundArmory.ID, TierSortingRegistry.getSortedTiers().contains(previous) ? TierSortingRegistry.getName(previous).toUnderscoreSeparatedString() : "base"),
				List.of(previous),
				Util2.cast(TierSortingRegistry.getSortedTiers().stream().filter(tier -> tier != previous && !lower.contains(tier)).toList())
			);

			ldc(() -> Invoker.findStatic(TierSortingRegistry.class, "recalculateItemTiers", void.class)).invoke();

			return material;
		});
	}
}
