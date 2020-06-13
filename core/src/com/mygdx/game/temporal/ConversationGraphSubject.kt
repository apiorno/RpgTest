package com.mygdx.game.temporal

import com.badlogic.gdx.utils.Array
import com.mygdx.game.dialog.ConversationGraph
import com.mygdx.game.temporal.ConversationGraphObserver.*

open class ConversationGraphSubject {
    private val observers: Array<ConversationGraphObserver> = Array()
    fun addObserver(graphObserver: ConversationGraphObserver) {
        observers.add(graphObserver)
    }

    fun removeObserver(graphObserver: ConversationGraphObserver) {
        observers.removeValue(graphObserver, true)
    }

    fun removeAllObservers() {
        for (observer in observers) {
            observers.removeValue(observer, true)
        }
    }

    fun notify(graph: ConversationGraph?, event: ConversationCommandEvent?) {
        for (observer in observers) {
            observer.onNotify(graph, event)
        }
    }

}
