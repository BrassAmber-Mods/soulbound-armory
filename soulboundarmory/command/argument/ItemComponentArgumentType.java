package soulboundarmory.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.util.Util2;

import java.util.List;
import java.util.stream.Stream;

public class ItemComponentArgumentType<C extends ItemComponent<C>> extends RegistryArgumentType<ItemComponentType<C>> {
	protected ItemComponentArgumentType() {
		super(ItemComponentType.registry());
	}

	public static <C extends ItemComponent<C>> RegistryArgumentType<ItemComponentType<C>> itemComponents() {
		return new ItemComponentArgumentType();
	}

	@Override public List<ItemComponentType<C>> parse(StringReader reader) {
		var cursor = reader.getCursor();

		if (Util2.containsIgnoreCase(reader.readString(), "current")) {
			return ReferenceArrayList.of();
		}

		reader.setCursor(cursor);
		return super.parse(reader);
	}

	@Override protected Stream<String> suggestions(CommandContext context, SuggestionsBuilder builder) {
		return Stream.concat(Stream.of("current"), super.suggestions(context, builder));
	}
}
