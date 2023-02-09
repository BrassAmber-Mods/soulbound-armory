package soulboundarmory.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.Int2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntPredicate;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import soulboundarmory.client.i18n.Translations;
import soulboundarmory.module.gui.widget.ItemWidget;
import soulboundarmory.module.gui.widget.TextWidget;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.skill.SkillInstance;

import java.util.Map;

/**
 The skill tab; design (not code of course) blatantly copied from the advancement screen.
 */
public class SkillTab extends Tab {
	// protected static final Identifier background = new Identifier("textures/block/cobbled_deepslate.png");

	protected final Map<SkillInstance, SkillWidget> skills = new Reference2ReferenceLinkedOpenHashMap<>();
	protected final Widget<?> container = new Widget<>().movable();

	protected float brightness = 1;

	public SkillTab() {
		super(Translations.guiSkills);
	}

	@Override public Widget<?> icon() {
		/*
		return new ScalableWidget<>()
			.texture(Util.id("textures/skill/shadow_clone.png"))
			.textureSize(16, 16)
			.slice(0, 16, 16, 0, 16, 16)
			.scaleMode(ScaleMode.STRETCH);
		*/
		return new ItemWidget().item(Items.PAPER);
	}

	@Override public void initialize() {
		if (!this.dim()) {
			this.brightness = 1;
		}

		this.updateWidgets();

		var points = this.add(new TextWidget())
			.x(0.5)
			.y(this.button.absoluteEndY())
			.centerX()
			.stroke()
			.text(() -> this.pointText(this.container().item().skillPoints()))
			.color(0xEEEEEE);

		var width = this.container.descendantWidth();

		this.add(this.container)
			.height(this.container.descendantHeight())
			.x(MathHelper.clamp((this.width() - width) / 2, 0, this.container().options.absoluteX() - 4 - width))
			.y(Math.max(points.endY() + 8, this.container().height() / 2))
			.centerY();
	}

	@Override protected void render() {
		var delta = 20 * tickDelta() / 255F;
		this.brightness = this.dim() ? Math.max(this.brightness - delta, 175 / 255F) : Math.min(this.brightness + delta, 1);
		brightness(this.brightness);
		RenderSystem.enableBlend();
		// this.renderBackground(background, 0, 0, this.width(), this.height(), (int) (128 * this.colorValue));
	}

	private boolean dim() {
		return this.container.children().anyMatch(child -> child.tooltips.stream().anyMatch(Widget::isPresent));
	}

	private void updateWidgets() {
		// 0: index of the current skill per tier (used in a loop)
		// 1: number of skills per tier
		var tierOrders = new Int2ReferenceLinkedOpenHashMap<int[]>();
		var skills = this.container().item().skills();

		for (var skill : skills) {
			tierOrders.computeIfAbsent(skill.tier(), tier -> new int[]{0, 0})[1]++;
		}

		IntPredicate truthy = n -> n != 0;
		var width = 2 + tierOrders.keySet().intStream()
			.filter(truthy).map(tier -> tierOrders.get(tier)[1])
			.filter(truthy).map(length -> length - 1)
			.sum();

		for (var skill : skills) {
			var tier = skill.tier();
			var data = tierOrders.computeIfAbsent(tier, t -> new int[]{0, 0});
			var spacing = skill.hasDependencies() ? 48 : width * 24;
			var x = data[0] * spacing;

			if (skill.hasDependencies()) {
				var dependencies = skill.dependencies;
				var offset = (1 - data[1]) * spacing / 2;
				x += offset + dependencies.stream().mapToInt(d -> this.skills.get(d).x()).sum() / dependencies.size();
			}

			this.skills.computeIfAbsent(skill, s -> this.container.add(new SkillWidget(this, s).size(24))).x(x).y(32 * tier);
			data[0]++;
		}
	}
}
