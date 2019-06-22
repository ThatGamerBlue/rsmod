package gg.rsmod.game.message.handler

import gg.rsmod.game.message.MessageHandler
import gg.rsmod.game.message.impl.EventMouseClickedMessage
import gg.rsmod.game.model.World
import gg.rsmod.game.model.entity.Client

class EventMouseClickedHandler : MessageHandler<EventMouseClickedMessage> {

	override fun handle(client: Client, world: World, message: EventMouseClickedMessage) {
		// TODO: implement this
	}
}
