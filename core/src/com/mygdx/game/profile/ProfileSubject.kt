package com.mygdx.game.profile

import com.badlogic.gdx.utils.Array
import com.mygdx.game.profile.ProfileObserver.ProfileEvent

open class ProfileSubject {
    private val observers: Array<ProfileObserver> = Array()
    fun addObserver(profileObserver: ProfileObserver) {
        observers.add(profileObserver)
    }

    fun removeObserver(profileObserver: ProfileObserver) {
        observers.removeValue(profileObserver, true)
    }

    fun removeAllObservers() {
        observers.removeAll(observers, true)
    }

    protected fun notify(profileManager: ProfileManager, event: ProfileEvent) {
        observers.forEach { it.onNotify(profileManager,event) }
    }

}