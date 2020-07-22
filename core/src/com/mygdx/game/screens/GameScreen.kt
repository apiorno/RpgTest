package com.mygdx.game.screens

import com.badlogic.gdx.utils.Array
import com.mygdx.game.audio.AudioManager
import com.mygdx.game.audio.AudioObserver
import com.mygdx.game.audio.AudioObserver.AudioCommand
import com.mygdx.game.audio.AudioObserver.AudioTypeEvent
import com.mygdx.game.audio.AudioSubject
import ktx.app.KtxScreen

open class GameScreen : KtxScreen, AudioSubject {
    private val observers: Array<AudioObserver> = Array()

    init {
        addObserver(AudioManager)
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
        observers.forEach { it.onNotify(command,event) }
    }

}