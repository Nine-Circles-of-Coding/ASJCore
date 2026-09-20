package gloomyfolken.hooklib.minecraft;

import com.KAIIIAK.KASMLib.util.KASMUtil;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import gloomyfolken.hooklib.asm.ClassMetadataReader;
import gloomyfolken.hooklib.asm.HookClassTransformer;
import net.minecraft.launchwrapper.*;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Еще больше костылей вдобавок к ClassMetadataReader для работы с майновской обфускацией.
 */
public class DeobfuscationMetadataReader extends ClassMetadataReader {
	
	@Override
	public byte[] getClassData(String className) throws IOException {
		String internalName = className.replace('.', '/');
		byte[] bytes = super.getClassData(unmap(internalName));
		return deobfuscateClass(internalName, bytes);
	}
	
	// Фордж и прочее могут своими патчами добавлять методы, которые нужно уметь оверрайдить хуками.
	// Для этого приходится применять трансформеры во время поиска супер-методов
	// этот метод должен вызываться только во время загрузки сабклассов проверяемого класса,
	// так что все должно быть норм
	@Override
	protected MethodReference getMethodReferenceASM(String type, String methodName, String desc) throws IOException {
		FindMethodClassVisitor cv = new FindMethodClassVisitor(methodName, desc);
		byte[] bytes = getTransformedBytes(type);
		acceptVisitor(bytes, cv);
		return cv.found ? new MethodReference(type, cv.targetName, cv.targetDesc) : null;
	}
	
	@Override
	protected boolean checkSameMethod(String sourceName, String sourceDesc, String targetName, String targetDesc) {
		return checkSameMethod(sourceName, targetName) && sourceDesc.equals(targetDesc);
	}
	
	private static boolean checkSameMethod(String srgName, String mcpName) {
		if (HookLibPlugin.getObfuscated() && MinecraftClassTransformer.instance != null) {
			int methodId = MinecraftClassTransformer.getMethodId(srgName);
			String remappedName = MinecraftClassTransformer.instance.getMethodNames().get(methodId);
			if (remappedName != null && remappedName.equals(mcpName)) {
				return true;
			}
		}
		return srgName.equals(mcpName);
	}
	
	private static final Deque<String> currentTransformChain = new ArrayDeque<>();
	
	private static byte[] getTransformedBytes(String type) throws IOException {
		String obfName = unmap(type);
		byte[] bytes = Launch.classLoader.getClassBytes(obfName);
		if (bytes == null) {
			throw new RuntimeException("Bytes for " + obfName + " not found");
		}
		
		// Вызов метода в котором мы находимся скорее всего происходит из трансформера, поэтому вызов runTransformers
		// без предостережений может создать бесконечный цикл трансформирования одного и того же класса.
		// Возвращаем оригинальные байты класса, если для него мы уже вызвали runTransformers, но снова оказались здесь
		if (currentTransformChain.contains(type)) {
			return bytes;
		}
		
		currentTransformChain.addLast(type);
		try {
			HookClassTransformer.skipTransformation = true;
			bytes = KASMUtil.applyAllPossibleTransformers(obfName.replace('/', '.'), type.replace('/', '.'), bytes);
		} catch (Exception e) {
			HookClassTransformer.logger.error("Error:", e);
		} finally {
			HookClassTransformer.skipTransformation = false;
		}
		currentTransformChain.removeLast();
		
		return bytes;
	}
	
	// возвращает из необфусцированного названия типа обфусцированное
	private static String unmap(String type) {
		if (HookLibPlugin.getObfuscated()) {
			return FMLDeobfuscatingRemapper.INSTANCE.unmap(type);
		}
		return type;
	}
	
	static byte[] deobfuscateClass(String type, byte[] bytes) {
		if (HookLoader.getDeobfuscationTransformer() != null) {
			bytes = HookLoader.getDeobfuscationTransformer().transform(type, type.replace('/', '.'), bytes);
		}
		return bytes;
	}
}
