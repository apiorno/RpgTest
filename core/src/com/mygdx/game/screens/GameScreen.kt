package com.mygdx.game.screens

import com.badlogic.gdx.Screen
import com.badlogic.gdx.utils.Array
import com.mygdx.game.audio.AudioManager
import com.mygdx.game.audio.AudioObserver
import com.mygdx.game.audio.AudioObserver.AudioCommand
import com.mygdx.game.audio.AudioObserver.AudioTypeEvent
import com.mygdx.game.audio.AudioSubject

open class GameScreen : Screen, AudioSubject {
    private val _observers: Array<AudioObserver> = Array()
    override fun addObserver(audioObserver: AudioObserver) {
        _observers.add(audioObserver)
    }

    override fun removeObserver(audioObserver: AudioObserver) {
        _observers.removeValue(audioObserver, true)
    }

    override fun removeAllObservers() {
        _observers.removeAll(_observers, true)
    }

    override fun notify(command: AudioCommand, event: AudioTypeEvent) {
        for (observer in _observers) {
            observer.onNotify(command, event)
        }
    }

    override fun show() {}
    override fun render(delta: Float) {}
    override fun resize(width: Int, height: Int) {}
    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
    override fun dispose() {}

    init {
        addObserver(AudioManager.instance)
    }
}