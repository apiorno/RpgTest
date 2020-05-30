package com.mygdx.game.audio

interface AudioSubject {
    fun addObserver(audioObserver: AudioObserver)
    fun removeObserver(audioObserver: AudioObserver)
    fun removeAllObservers()
    fun notify(command: AudioObserver.AudioCommand, event: AudioObserver.AudioTypeEvent)
}