package alexsocol.patcher.asm.worker

import com.KAIIIAK.KASMLib.KASMWorker
import org.objectweb.asm.tree.ClassNode

object InterfaceAppenderWorker: KASMWorker() {
	
	val additionalInterfaces = HashMap<String, HashSet<String>>()
	
	@JvmStatic
	@Suppress("unused")
	fun registerAdditionalInterface(target: String, iface: String) {
		additionalInterfaces.computeIfAbsent(target) { HashSet() }.add(iface)
	}
	
	override fun workClass(cn: ClassNode): Boolean {
		val ifaces = additionalInterfaces[cn.name] ?: return false
		cn.interfaces.addAll(ifaces)
		
		return false
	}
}