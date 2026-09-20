package alexsocol.patcher.asm.worker

import com.KAIIIAK.KASMLib.*
import org.objectweb.asm.tree.ClassNode

object InterfaceAppenderWorker: KASMWorker() {
	
	val additionalInterfaces = HashMap<String, HashSet<String>>()
	
	@JvmStatic
	fun registerAdditionalInterface(target: String, iface: String) {
		additionalInterfaces.computeIfAbsent(target.replace('.', '/')) { HashSet() }.add(iface.replace('.', '/'))
	}
	
	override fun workClass(cn: ClassNode): Boolean {
		val ifaces = additionalInterfaces[cn.name.replace('.', '/')] ?: return false
		
		KASMLib.logger.debug("Appending interface list $ifaces to ${cn.name}")
		
		cn.interfaces.addAll(ifaces)
		changes++
		
		return false
	}
}