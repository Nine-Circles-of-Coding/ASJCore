package alexsocol.asjlib.command

import net.minecraft.command.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.potion.*
import net.minecraft.util.EnumChatFormatting

object CommandHeal : CommandBase() {

  override fun getCommandName() = "heal"

  override fun getCommandUsage(sender: ICommandSender?) =
      "/$commandName [[player]] [${EnumChatFormatting.UNDERLINE}h${EnumChatFormatting.RED}(heal player)][${EnumChatFormatting.UNDERLINE}f${EnumChatFormatting.RED}(feed player)][${EnumChatFormatting.UNDERLINE}e${EnumChatFormatting.RED}(clear debuffs)[${EnumChatFormatting.UNDERLINE}!${EnumChatFormatting.RED}(clear ALL effects)]]"

  override fun processCommand(sender: ICommandSender, args: Array<String>) {
    if (args.size > 2) throw WrongUsageException(getCommandUsage(sender))

    var heal: Boolean
    var feed: Boolean
    var effs: Boolean
    var eAll: Boolean

    args
        .getOrElse(if (args.size == 2) 1 else 0) { "hfe" }
        .lowercase()
        .apply {
          if (contains("[^hfe!]+".toRegex())) throw WrongUsageException("Invalid flags")

          heal = contains('h')
          feed = contains('f')
          effs = contains('e')
          eAll = contains("e!")
        }

    if (!heal && !feed && !effs) throw WrongUsageException("Invalid flags")

    val target =
        if (args.size == 2) getPlayer(sender, args[0])
        else sender as? EntityPlayer ?: throw WrongUsageException("Target is not player")

    if (heal) target.heal(target.maxHealth)
    if (feed) target.foodStats.addStats(20, 20f)
    if (effs)
        target.activePotionEffects.removeAll {
          val flag = Potion.potionTypes[(it as PotionEffect).potionID].isBadEffect || eAll
          if (flag) target.onFinishedPotionEffect(it)
          flag
        }
  }
}
