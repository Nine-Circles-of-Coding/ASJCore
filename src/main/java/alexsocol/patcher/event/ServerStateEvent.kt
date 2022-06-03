package alexsocol.patcher.event

import cpw.mods.fml.common.event.*
import cpw.mods.fml.common.eventhandler.Event

abstract class ServerStateEvent: Event()

class ServerStartingEvent(val event: FMLServerStartingEvent): ServerStateEvent()
class ServerStartedEvent(val event: FMLServerStartedEvent): ServerStateEvent()
class ServerStoppingEvent(val event: FMLServerStoppingEvent): ServerStateEvent()
class ServerStoppedEvent(val event: FMLServerStoppedEvent): ServerStateEvent()

val ServerStartingEvent.save: String get() = event.server.entityWorld.saveHandler.worldDirectory.absolutePath