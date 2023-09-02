package soulboundarmory.event;

import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableTextContent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import soulboundarmory.SoulboundArmory;
import soulboundarmory.client.gui.bar.ExperienceBar;
import soulboundarmory.client.gui.screen.SoulboundScreen;
import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.config.Configuration;
import soulboundarmory.item.SoulboundItem;
import soulboundarmory.module.gui.screen.ScreenWidget;
import soulboundarmory.module.gui.widget.Widget;

import java.util.stream.IntStream;

@EventBusSubscriber(value = Dist.CLIENT, modid = SoulboundArmory.ID)
public final class ClientEvents {
	@SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
		ItemComponent.of(event.getEntity(), event.getItemStack()).ifPresent(component -> {
			var tooltip = event.getToolTip();
			var startIndex = 1 + IntStream.range(0, tooltip.size())
				.filter(index -> tooltip.get(index).getContent() instanceof TranslatableTextContent translation && translation.getKey().equals("item.modifiers.mainhand"))
				.sum();

			IntStream.range(startIndex, tooltip.size()).forEach(x -> tooltip.remove(startIndex));
			tooltip.addAll(component.tooltip());
		});
	}

	@SubscribeEvent public static void hideSoulboundItemWhileTransforming(RenderHandEvent event) {
		if (summoning(event.getItemStack())) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent public static void renderSoulboundItemExperienceBar(RenderGuiOverlayEvent.Pre event) {
		if (event.getOverlay() == VanillaGuiOverlay.EXPERIENCE_BAR.type())
			if (ScreenWidget.cellScreen() instanceof SoulboundScreen screen && screen.xpBar.isVisible() || Configuration.Client.overlayExperienceBar && ExperienceBar.renderOverlay(event.getPoseStack())) {
				event.setCanceled(true);
			}
	}

	public static boolean summoning(ItemStack stack) {
		var floating = Widget.gameRenderer.floatingItem;
		return floating != null && floating.getItem() instanceof SoulboundItem && stack.isItemEqualIgnoreDamage(floating);
	}
}
