package com.mygdx.game.windows

import com.badlogic.gdx.Screen
import com.badlogic.gdx.utils.Array
import com.mygdx.game.audio.AudioManager
import com.mygdx.game.audio.AudioObserver
import com.mygdx.game.audio.AudioObserver.*
import com.mygdx.game.audio.AudioSubject

open class GameScreen : Screen, AudioSubject {

    private val observers: Array<AudioObserver> = Array()

    init {
        addObserver(AudioManager.instance!!)
    }
    override fun addObserver(audioObserver: AudioObserver) {
        observers.add(audioObserver)
    }

    override fun removeObserver(audioObserver: AudioObserver) {
        observers.removeValue(audioObserver, true)
    }

    override fun removeAllObservers() {
        observers.removeAll(observers, true)
    }

    override fun notify(command: AudioCommand, event: AudioTypeEvent) {
        observers.forEach { it.onNotify(command, event)  }
    }

    override fun show() {}
    override fun render(delta: Float) {}
    override fun resize(width: Int, height: Int) {}
    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
    override fun dispose() {}
}