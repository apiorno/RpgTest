package com.mygdx.game.maps

import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.utils.Array
import com.mygdx.game.audio.AudioManager
import com.mygdx.game.audio.AudioObserver
import com.mygdx.game.audio.AudioSubject

abstract class MapContentController : AudioSubject {
    abstract val mapType: MapFactory.MapType
    private val observers: Array<AudioObserver> = Array()

    abstract fun unloadMusic()
    abstract fun loadMusic()
    abstract fun loadMap() : TiledMap?

    override fun addObserver(audioObserver: AudioObserver) {
        observers.add(audioObserver)
    }

    override fun removeObserver(audioObserver: AudioObserver) {
        observers.removeValue(audioObserver, true)
    }

    override fun removeAllObservers() {
        observers.removeAll(observers, true)
    }

    override fun notify(command: AudioObserver.AudioCommand, event: AudioObserver.AudioTypeEvent) {
        for (observer in observers) {
            observer.onNotify(command, event)
        }
    }

}