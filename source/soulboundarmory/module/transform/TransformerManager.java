package soulboundarmory.module.transform;

import net.auoeke.reflect.ClassTransformer;
import net.auoeke.reflect.Reflect;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.Instrumentation;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TransformerManager {
	private static final Instrumentation instrumentation = Reflect.instrument().value();

	public static void addSingleUseTransformer(String targetName, Consumer<ClassNode> transformer) {
		instrumentation.addTransformer(transformer(node -> {
			transformer.accept(node);
			return true;
		}).ofType(targetName).singleUse(instrumentation), true);
	}

	public static void addTransformer(Predicate<ClassNode> transformer) {
		instrumentation.addTransformer(transformer(transformer));
	}

	private static ClassTransformer transformer(Predicate<ClassNode> transformer) {
		return ClassTransformer.of((module, loader, name, type, domain, classFile) -> {
			var node = new ClassNode();
			new ClassReader(classFile).accept(node, 0);

			if (transformer.test(node)) {
				var writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
				node.accept(writer);

				return writer.toByteArray();
			}

			return null;
		}).exceptionLogging();
	}
}
