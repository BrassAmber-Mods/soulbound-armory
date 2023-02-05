package soulboundarmory.network;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraftforge.registries.IForgeRegistry;
import soulboundarmory.component.soulbound.item.ItemComponent;
import soulboundarmory.component.soulbound.item.ItemComponentType;
import soulboundarmory.component.soulbound.player.MasterComponent;
import soulboundarmory.module.gui.widget.Widget;
import soulboundarmory.registry.Identifiable;
import soulboundarmory.util.Util;
import soulboundarmory.util.Util2;

public class ExtendedPacketBuffer extends PacketByteBuf {
	public ExtendedPacketBuffer() {
		super(Unpooled.buffer());
	}

	public ExtendedPacketBuffer(ByteBuf buffer) {
		super(buffer);
	}

	public ExtendedPacketBuffer(MasterComponent<?> component) {
		this();

		this.writeIdentifier(component.key().id);
	}

	public ExtendedPacketBuffer(ItemComponent<?> component) {
		this();

		this.writeItemComponent(component);
	}

	public ExtendedPacketBuffer writeRegistryEntry(Identifiable entry) {
		this.writeIdentifier(entry.id());

		return this;
	}

	public <T> T readRegistryEntry(IForgeRegistry<T> registry) {
		return registry.getValue(this.readIdentifier());
	}

	@Override public ExtendedPacketBuffer writeBoolean(boolean value) {
		super.writeBoolean(value);

		return this;
	}

	@Override public Identifier readIdentifier() {
		return new Identifier(this.readString());
	}

	@Override public ExtendedPacketBuffer writeIdentifier(Identifier identifier) {
		return this.writeString(identifier.toString());
	}

	@Override public String readString() {
		return this.readCharSequence(this.readInt(), StandardCharsets.UTF_8).toString();
	}

	@Override public ExtendedPacketBuffer writeString(String string) {
		this.writeInt(string.length());
		this.writeCharSequence(string, StandardCharsets.UTF_8);

		return this;
	}

	@Override public ExtendedPacketBuffer writeItemStack(ItemStack itemStack) {
		return (ExtendedPacketBuffer) super.writeItemStack(itemStack, false);
	}

	@Override public ExtendedPacketBuffer writeUuid(UUID id) {
		return (ExtendedPacketBuffer) super.writeUuid(id);
	}

	@Override public ExtendedPacketBuffer writeInt(int value) {
		super.writeInt(value);

		return this;
	}

	@Override public ExtendedPacketBuffer writeByte(int B) {
		super.writeByte(B);

		return this;
	}

	public ExtendedPacketBuffer writeEntity(Entity entity) {
		return this.writeInt(entity.getId());
	}

	public <T extends Entity> Optional<T> readEntity() {
		var id = this.readInt();
		return Util.isClient()
			? Optional.ofNullable((T) Widget.client.world.getEntityById(id))
			: Util2.stream(Util.server().getWorlds()).map(world -> (T) world.getEntityById(id)).filter(Objects::nonNull).findAny();
	}

	public ExtendedPacketBuffer writeItemComponent(ItemComponent<?> component) {
		this.writeIdentifier(component.type().id());

		return this;
	}

	public ItemComponent<?> readItemComponent(PlayerEntity player) {
		return ItemComponentType.get(this.readIdentifier()).of(player);
	}

	@Override public ExtendedPacketBuffer writeNbt(NbtCompound tag) {
		super.writeNbt(tag);

		return this;
	}
}
