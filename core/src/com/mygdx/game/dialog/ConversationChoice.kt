package com.mygdx.game.dialog

import com.mygdx.game.dialog.ConversationGraphObserver.ConversationCommandEvent

class ConversationChoice( var sourceId: String, var destinationId: String,  var choicePhrase: String) {

    var conversationCommandEvent: ConversationCommandEvent? = null

    override fun toString(): String {
        return choicePhrase
    }
}