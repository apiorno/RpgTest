package com.mygdx.game.dialog

import com.mygdx.game.dialog.ConversationGraphObserver.ConversationCommandEvent
import ktx.collections.GdxArray

open class ConversationGraphSubject {
    private val observers: GdxArray<ConversationGraphObserver> = GdxArray()

    fun addObserver(graphObserver: ConversationGraphObserver) {
        observers.add(graphObserver)
    }

    fun removeObserver(graphObserver: ConversationGraphObserver) {
        observers.removeValue(graphObserver, true)
    }

    fun removeAllObservers() {
        observers.clear()
    }

    fun notify(graph: ConversationGraph, event: ConversationCommandEvent) {
        observers.forEach { it.onNotify(graph, event) }
    }
}