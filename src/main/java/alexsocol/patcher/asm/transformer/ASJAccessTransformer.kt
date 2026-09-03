package alexsocol.patcher.asm.transformer

import cpw.mods.fml.common.asm.transformers.AccessTransformer

class ASJAccessTransformer: AccessTransformer("inapplicable-at-list")
class ASJAccessTransformerDev: AccessTransformer("META-INF/asjlib_at.cfg")