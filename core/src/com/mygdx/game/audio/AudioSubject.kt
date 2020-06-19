package com.mygdx.game.audio

import com.mygdx.game.audio.AudioObserver.AudioCommand
import com.mygdx.game.audio.AudioObserver.AudioTypeEvent

interface AudioSubject {
    fun addObserver(audioObserver: AudioObserver)
    fun removeObserver(audioObserver: AudioObserver)
    fun removeAllObservers()
    fun notify(command: AudioCommand, event: AudioTypeEvent)
}