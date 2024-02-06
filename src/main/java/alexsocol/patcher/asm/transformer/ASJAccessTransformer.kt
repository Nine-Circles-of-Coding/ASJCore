package alexsocol.patcher.asm.transformer

import alexsocol.patcher.asm.ASJHookLoader
import cpw.mods.fml.common.asm.transformers.AccessTransformer

class ASJAccessTransformer: AccessTransformer("inapplicable-at-list") {
	
	override fun transform(name: String?, transformedName: String?, bytes: ByteArray?) =
		if (ASJHookLoader.OBF) super.transform(name, transformedName, bytes) else bytes
}