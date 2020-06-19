package com.mygdx.game.dialog

import com.badlogic.gdx.utils.Array
import com.mygdx.game.dialog.ConversationGraphObserver.ConversationCommandEvent

open class ConversationGraphSubject {
    private val _observers: Array<ConversationGraphObserver>
    fun addObserver(graphObserver: ConversationGraphObserver) {
        _observers.add(graphObserver)
    }

    fun removeObserver(graphObserver: ConversationGraphObserver) {
        _observers.removeValue(graphObserver, true)
    }

    fun removeAllObservers() {
        for (observer in _observers) {
            _observers.removeValue(observer, true)
        }
    }

    fun notify(graph: ConversationGraph, event: ConversationCommandEvent) {
        for (observer in _observers) {
            observer.onNotify(graph, event)
        }
    }

    init {
        _observers = Array()
    }
}