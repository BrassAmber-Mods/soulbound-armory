package soulboundarmory.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.UUID;

public final class AttributeModifierIdentifiers {
	public static final UUID reach = UUID.fromString("2D4AA65A-4A15-4C46-9F6B-D3898AEC42B6");
	public static final ObjectOpenHashSet<UUID> reserved = ObjectOpenHashSet.of(reach);
}
