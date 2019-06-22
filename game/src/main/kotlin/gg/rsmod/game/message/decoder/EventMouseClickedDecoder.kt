package gg.rsmod.game.message.decoder

import gg.rsmod.game.message.MessageDecoder
import gg.rsmod.game.message.impl.EventMouseClickedMessage

class EventMouseClickedDecoder : MessageDecoder<EventMouseClickedMessage>() {
    override fun decode(opcode: Int, opcodeIndex: Int, values: HashMap<String, Number>, stringValues: HashMap<String, String>): EventMouseClickedMessage {
        val wasRightClick = values["tickandtype"]!!.toInt() and 1 == 1
        val clientTick = values["tickandtype"]!!.toInt() shr 1
        val mouseX = values["x"]!!.toInt()
        val mouseY = values["y"]!!.toInt()
        return EventMouseClickedMessage(wasRightClick, clientTick, mouseX, mouseY)
    }
}