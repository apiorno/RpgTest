package com.mygdx.game.widgets

import com.mygdx.game.widgets.StatusObserver.StatusEvent

interface StatusSubject {
    fun addObserver(statusObserver: StatusObserver)
    fun removeObserver(statusObserver: StatusObserver)
    fun removeAllObservers()
    fun notify(value: Int, event: StatusEvent)
}