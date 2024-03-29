package dev;

import net.auoeke.reflect.Accessor;
import net.minecraft.server.command.CommandManager;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.spongepowered.tools.agent.MixinAgent;
import soulboundarmory.SoulboundArmory;
import soulboundarmory.module.transform.TransformerManager;

import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.GETSTATIC;

@EventBusSubscriber(modid = SoulboundArmory.ID)
public class DevEvents {
	@SubscribeEvent
	public static void registerCommand(RegisterCommandsEvent event) {
		Accessor.<Logger>getReference(Accessor.<Object>getReference(CommandManager.class, "LOGGER"), "logger").setLevel(Level.DEBUG);
	}

	static {
		MixinAgent.agentmain(null, TransformerManager.instrumentation);

		TransformerManager.addSingleUseTransformer("com/mojang/text2speech/Narrator", node -> {
			var getNarrator = node.methods.stream().filter(method -> method.name.equals("getNarrator")).findAny().get();
			getNarrator.instructions.clear();
			getNarrator.tryCatchBlocks.clear();
			getNarrator.visitFieldInsn(GETSTATIC, node.name, "EMPTY", 'L' + node.name + ';');
			getNarrator.visitInsn(ARETURN);
		});
	}
}
