package alexsocol.asjlib.extendables

import alexsocol.asjlib.preconfig.PreConfiguration
import cpw.mods.fml.relauncher.IFMLLoadingPlugin
import java.io.File

abstract class ASJPreConfigHandler {
	
	lateinit var preconfig: PreConfiguration
	
	/**
	 * Function to be called once in [IFMLLoadingPlugin] <clinit>/<init>
	 * to initialize categories and properties
	 */
	fun loadPreConfig(cfg: File) {
		preconfig = PreConfiguration(cfg)
		preconfig.load()
		addCategories()
		syncConfig()
	}
	
	fun addCategory(cat: String, comment: String) {
		preconfig.addCustomCategoryComment(cat, comment)
		preconfig.getCategory(cat).setLanguageKey(cat)
	}
	
	fun syncConfig() {
		readProperties()
		
		if (preconfig.hasChanged()) preconfig.save()
	}
	
	open fun addCategories() = Unit
	
	abstract fun readProperties()
	
	fun loadProp(category: String, propName: String, default: Boolean, restart: Boolean, desc: String?): Boolean {
		val prop = preconfig.get(category, propName, default, desc)
		prop.setRequiresMcRestart(restart)
		return prop.getBoolean(default)
	}
	
	@JvmOverloads
	fun loadProp(category: String, propName: String, default: Int, restart: Boolean, desc: String?, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE): Int {
		val prop = preconfig.get(category, propName, default, desc, min, max)
		prop.setRequiresMcRestart(restart)
		return prop.getInt(default).also { if (it !in min..max) throw IllegalArgumentException("Int $propName is not within required min/max bounds ($it), must be in range $min..$max") }
	}
	
	@JvmOverloads
	fun loadProp(category: String, propName: String, default: IntArray, restart: Boolean, desc: String?, ensureLength: Boolean = true): IntArray {
		val prop = preconfig.get(category, propName, default, desc)
		prop.setRequiresMcRestart(restart)
		return prop.intList.also { if (ensureLength && it.size < default.size) throw IllegalArgumentException("Array $propName is not of suitable length (${it.size}), must be ${default.size}") }
	}
	
	@JvmOverloads
	fun loadProp(category: String, propName: String, default: Double, restart: Boolean, desc: String?, min: Double = -Double.MIN_VALUE, max: Double = Double.MAX_VALUE): Double {
		val prop = preconfig.get(category, propName, default, desc, min, max)
		prop.setRequiresMcRestart(restart)
		return prop.getDouble(default)
	}
	
	@JvmOverloads
	fun loadProp(category: String, propName: String, default: DoubleArray, restart: Boolean, desc: String?, ensureLength: Boolean = true): DoubleArray {
		val prop = preconfig.get(category, propName, default, desc)
		prop.setRequiresMcRestart(restart)
		return prop.doubleList.also { if (ensureLength && it.size < default.size) throw IllegalArgumentException("Array $propName is not of suitable length (${it.size}), must be ${default.size}") }
	}
	
	fun loadProp(category: String, propName: String, default: String, restart: Boolean, desc: String?): String {
		val prop = preconfig.get(category, propName, default, desc)
		prop.setRequiresMcRestart(restart)
		return prop.string
	}
	
	@JvmOverloads
	fun loadProp(category: String, propName: String, default: Array<String>, restart: Boolean, desc: String?, ensureLength: Boolean = true): Array<String> {
		val prop = preconfig.get(category, propName, default, desc)
		prop.setRequiresMcRestart(restart)
		return prop.stringList.also { if (ensureLength && it.size < default.size) throw IllegalArgumentException("Array $propName is not of suitable length (${it.size}), must be ${default.size}") }
	}
}
