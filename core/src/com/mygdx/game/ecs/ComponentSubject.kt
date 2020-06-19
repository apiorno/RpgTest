package com.mygdx.game.ecs

import com.badlogic.gdx.utils.Array
import com.mygdx.game.ecs.ComponentObserver

open class ComponentSubject {
    private val _observers: Array<ComponentObserver>
    fun addObserver(conversationObserver: ComponentObserver) {
        _observers.add(conversationObserver)
    }

    fun removeObserver(conversationObserver: ComponentObserver) {
        _observers.removeValue(conversationObserver, true)
    }

    fun removeAllObservers() {
        for (observer in _observers) {
            _observers.removeValue(observer, true)
        }
    }

    protected fun notify(value: String, event: ComponentObserver.ComponentEvent) {
        for (observer in _observers) {
            observer.onNotify(value, event)
        }
    }

    init {
        _observers = Array()
    }
}