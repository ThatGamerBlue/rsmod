package gg.rsmod.game.message.impl

import gg.rsmod.game.message.Message

data class EventMouseClickedMessage(val wasRightClick: Boolean, val clientTick: Int, val x: Int, val y: Int) : Message
