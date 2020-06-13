package com.mygdx.game.dialog

import com.mygdx.game.temporal.ConversationGraphObserver.*

class ConversationChoice {
    var sourceId: String? = null
    var destinationId: String? = null
    var choicePhrase: String? = null
    var conversationCommandEvent: ConversationCommandEvent? = null

    override fun toString(): String {
        return choicePhrase!!
    }
}