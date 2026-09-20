package alexsocol.patcher.asm

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader
import com.gtnewhorizon.gtnhmixins.LateMixin
import net.minecraft.launchwrapper.Launch

/**
 * Registers the compat mixins - the ones aimed at other mods' classes - only when the target class
 * is actually on the classpath.
 *
 * Keyed on the target class rather than a mod id on purpose. It is exactly what HookLib did (a hook
 * whose target never loaded simply never applied), it cannot be silently wrong the way a mistyped
 * mod id can, and it copes with forks that ship the same classes under a different mod id.
 *
 * [presence] deliberately uses a resource lookup, never `Class.forName`: resolving the class here
 * would define and transform it before its mixin is registered, so the mixin would never apply.
 *
 * Entries are relative to the `alexsocol.mixins` package declared in mixins.asjlib.late.json.
 */
@LateMixin
class ASJLateMixins: ILateMixinLoader {

	override fun getMixinConfig() = "mixins.asjlib.late.json"
		
	override fun getMixins(loadedMods: MutableSet<String>): List<String> {
		val mixins = mutableListOf<String>()
		
		fun ifPresent(targetClass: String, mixin: String) {
			if (presence(targetClass)) mixins += mixin
		}
		
		// ISpecialArmor.getProperties fixes: ignore armour reduction for unblockable damage.
		ifPresent("ic2.core.item.armor.ItemArmorHazmat", "compat.MixinItemArmorHazmat")
		ifPresent("mods.battlegear2.items.ItemKnightArmour", "compat.MixinItemKnightArmour")
		ifPresent("thaumic.tinkerer.common.item.kami.armor.ItemIchorclothArmor", "compat.MixinItemIchorclothArmor")
		ifPresent("com.emoniph.witchery.item.ItemHunterClothes", "compat.MixinItemHunterClothes")
		ifPresent("com.emoniph.witchery.item.ItemVampireClothes", "compat.MixinItemVampireClothes")
		ifPresent("ab.common.item.equipment.armor.ItemNebulaArmor", "compat.MixinItemNebulaArmor")
		ifPresent("tconstruct.gadgets.item.ItemSlimeBoots", "compat.MixinItemSlimeBoots")
		ifPresent("gravisuite.ItemAdvancedJetPack", "compat.MixinItemAdvancedJetPack")
		ifPresent("gravisuite.ItemAdvancedLappack", "compat.MixinItemAdvancedLappack")
		ifPresent("gravisuite.ItemAdvancedNanoChestPlate", "compat.MixinItemAdvancedNanoChestPlate")
		ifPresent("gravisuite.ItemGraviChestPlate", "compat.MixinItemGraviChestPlate")
		ifPresent("net.mcft.copy.betterstorage.item.cardboard.ItemCardboardArmor", "compat.MixinItemCardboardArmor")
		ifPresent("net.mcft.copy.betterstorage.item.ItemBackpack", "compat.MixinItemBackpack")
		ifPresent("thaumrev.item.armor.ItemWardenArmor", "compat.MixinItemWardenArmor")

		// Top/bottom buttons, Forge Multipart half. The vanilla half is an early mixin.
		ifPresent("codechicken.multipart.minecraft.ButtonPart", "compat.MixinButtonPart")
		
		// Reach distance: stop other mods managing it themselves.
		ifPresent("vazkii.botania.common.core.proxy.CommonProxy", "compat.MixinBotaniaCommonProxy")
		ifPresent("vazkii.botania.client.core.proxy.ClientProxy", "compat.MixinBotaniaClientProxy")
		ifPresent("vazkii.botania.common.item.equipment.bauble.ItemReachRing", "compat.MixinItemReachRing")
		ifPresent("jp.mc.ancientred.starminer.core.entity.EntityLivingGravitized", "compat.MixinEntityLivingGravitized")
		ifPresent("com.gildedgames.the_aether.client.renders.AetherEntityRenderer", "compat.MixinAetherEntityRenderer")
		ifPresent("Reika.ChromatiCraft.Auxiliary.Ability.AbilityCalls", "compat.MixinAbilityCalls")
		ifPresent("mods.battlegear2.BattlemodeHookContainerClass", "compat.MixinBattlemodeHookContainerClass")
		ifPresent("mods.battlegear2.items.ItemDagger", "compat.MixinB2ItemDagger")
		ifPresent("mods.battlegear2.items.ItemSpear", "compat.MixinB2ItemSpear")
		ifPresent("minefantasy.mf2.mechanics.ExtendedReachMF", "compat.MixinExtendedReachMF")

		// CoFH re-routes pane connection through its own coremod class; see MixinHooksCore.
		ifPresent("cofh.asmhooks.HooksCore", "compat.MixinHooksCore")
		
		// Misc one-offs.
		ifPresent("biomesoplenty.common.blocks.BlockBOPLog", "compat.MixinBlockBOPLog")
		ifPresent("biomesoplenty.common.itemblocks.ItemBlockLog", "compat.MixinItemBlockLog")
		ifPresent("com.emoniph.witchery.dimension.WorldProviderDreamWorld", "compat.MixinWorldProviderDreamWorld")

		return mixins
	}

	private fun presence(className: String) =
		Launch.classLoader.getResource("${className.replace('.', '/')}.class") != null
}
