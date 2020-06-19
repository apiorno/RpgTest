package com.mygdx.game.profile

import com.badlogic.gdx.utils.Array
import com.mygdx.game.profile.ProfileObserver.ProfileEvent

open class ProfileSubject {
    private val _observers: Array<ProfileObserver>
    fun addObserver(profileObserver: ProfileObserver) {
        _observers.add(profileObserver)
    }

    fun removeObserver(profileObserver: ProfileObserver) {
        _observers.removeValue(profileObserver, true)
    }

    fun removeAllObservers() {
        _observers.removeAll(_observers, true)
    }

    protected fun notify(profileManager: ProfileManager, event: ProfileEvent) {
        for (observer in _observers) {
            observer.onNotify(profileManager, event)
        }
    }

    init {
        _observers = Array()
    }
}