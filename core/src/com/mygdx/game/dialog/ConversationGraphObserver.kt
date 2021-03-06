package com.mygdx.game.dialog

interface ConversationGraphObserver {
    enum class ConversationCommandEvent {
        LOAD_STORE_INVENTORY, EXIT_CONVERSATION, ACCEPT_QUEST, ADD_ENTITY_TO_INVENTORY, RETURN_QUEST, NONE
    }

    fun onNotify(graph: ConversationGraph, event: ConversationCommandEvent)
}