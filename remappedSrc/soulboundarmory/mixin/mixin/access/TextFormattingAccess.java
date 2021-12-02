package soulboundarmory.mixin.mixin.access;

import java.util.Map;
import java.util.regex.Pattern;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@SuppressWarnings({"ConstantConditions", "unused"})
@Mixin(Formatting.class)
public interface TextFormattingAccess {
    @Accessor("STRIP_FORMATTING_PATTERN")
    static Pattern pattern() {
        throw null;
    }

    @Accessor("FORMATTING_BY_NAME")
    static Map<String, Formatting> nameMap() {
        throw null;
    }

    @Invoker("cleanName")
    static String sanitize(String name) {
        throw null;
    }
}