package com.mygdx.game.temporal

interface StatusSubject {
    fun addObserver(statusObserver: StatusObserver)
    fun removeObserver(statusObserver: StatusObserver)
    fun removeAllObservers()
    fun notify(value: Int, event: StatusObserver.StatusEvent?)
}