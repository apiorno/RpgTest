package com.mygdx.game.audio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.mygdx.game.Utility
import java.util.*

class AudioManager : AudioObserver {
    private val queuedMusic: Hashtable<String, Music> = Hashtable()
    private val queuedSounds: Hashtable<String, Sound> = Hashtable()

    override fun onNotify(command: AudioObserver.AudioCommand?, event: AudioObserver.AudioTypeEvent) {
        when (command) {
            AudioObserver.AudioCommand.MUSIC_LOAD -> Utility.loadMusicAsset(event.value)
            AudioObserver.AudioCommand.MUSIC_PLAY_ONCE -> playMusic(false, event.value)
            AudioObserver.AudioCommand.MUSIC_PLAY_LOOP -> playMusic(true, event.value)
            AudioObserver.AudioCommand.MUSIC_STOP -> {
                val music = queuedMusic[event.value]
                music?.stop()
            }
            AudioObserver.AudioCommand.MUSIC_STOP_ALL -> for (musicStop in queuedMusic.values) {
                musicStop!!.stop()
            }
            AudioObserver.AudioCommand.SOUND_LOAD -> Utility.loadSoundAsset(event.value)
            AudioObserver.AudioCommand.SOUND_PLAY_LOOP -> playSound(true, event.value)
            AudioObserver.AudioCommand.SOUND_PLAY_ONCE -> playSound(false, event.value)
            AudioObserver.AudioCommand.SOUND_STOP -> {
                val sound = queuedSounds[event.value]
                sound?.stop()
            }
            else -> {
            }
        }
    }

    private fun playMusic(isLooping: Boolean, fullFilePath: String?): Music? {
        var music = queuedMusic[fullFilePath]
        if (music != null) {
            music.isLooping = isLooping
            music.play()
        } else if (Utility.isAssetLoaded(fullFilePath)) {
            music = Utility.getMusicAsset(fullFilePath!!)
            music!!.isLooping = isLooping
            music.play()
            queuedMusic[fullFilePath] = music
        } else {
            Gdx.app.debug(TAG, "Music not loaded")
            return null
        }
        return music
    }

    private fun playSound(isLooping: Boolean, fullFilePath: String?): Sound? {
        var sound = queuedSounds[fullFilePath]
        if (sound != null) {
            val soundId = sound.play()
            sound.setLooping(soundId, isLooping)
        } else if (Utility.isAssetLoaded(fullFilePath)) {
            sound = Utility.getSoundAsset(fullFilePath!!)
            val soundId = sound!!.play()
            sound.setLooping(soundId, isLooping)
            queuedSounds[fullFilePath] = sound
        } else {
            Gdx.app.debug(TAG, "Sound not loaded")
            return null
        }
        return sound
    }

    fun dispose() {
        for (music in queuedMusic.values) {
            music!!.dispose()
        }
        for (sound in queuedSounds.values) {
            sound!!.dispose()
        }
    }

    companion object {
        private val TAG = AudioManager::class.java.simpleName
        private var uniqueInstance: AudioManager? = null
        @JvmStatic
        val instance: AudioManager?
            get() {
                if (uniqueInstance == null) {
                    uniqueInstance = AudioManager()
                }
                return uniqueInstance
            }
    }


}