package soulboundarmory.module.component;

import net.minecraft.nbt.NbtCompound;
import soulboundarmory.serial.Serializable;

/**
 A persistent attachment to a game object.

 @param <T> the type of the implementing component
 */
public interface Component<C extends Component<C>> extends Serializable {
	/**
	 Invoked at the beginning of every tick.
	 */
	default void tickStart() {}

	/**
	 Serialize this component into `tag`.

	 @param tag an empty compound tag for this component
	 */
	@Override default void serialize(NbtCompound tag) {}

	/**
	 Deserialize this component from `tag`.
	 Invoked only if this component has been previously {@linkplain #serialize() serialized} and its tag is not empty.

	 @param tag a tag containing the same information from the last call to {@link #serialize()} on this component
	 */
	@Override default void deserialize(NbtCompound tag) {}

	/**
	 Invoked after the object is copied if it has the same component attached.
	 The default implementation deserializes the object's copy's component instance from this instance.
	 */
	default void copy(C copy) {
		var tag = this.serialize();

		if (!tag.isEmpty()) {
			copy.deserialize(tag);
		}
	}
}
