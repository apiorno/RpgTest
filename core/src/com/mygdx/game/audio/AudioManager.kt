package com.mygdx.game.audio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.mygdx.game.Utility.getMusicAsset
import com.mygdx.game.Utility.getSoundAsset
import com.mygdx.game.Utility.isAssetLoaded
import com.mygdx.game.Utility.loadMusicAsset
import com.mygdx.game.Utility.loadSoundAsset
import com.mygdx.game.audio.AudioObserver.AudioCommand
import com.mygdx.game.audio.AudioObserver.AudioTypeEvent
import java.util.*

class AudioManager private constructor() : AudioObserver {
    private val _queuedMusic: Hashtable<String?, Music?> = Hashtable()
    private val _queuedSounds: Hashtable<String?, Sound?> = Hashtable()
    override fun onNotify(command: AudioCommand?, event: AudioTypeEvent) {
        when (command) {
            AudioCommand.MUSIC_LOAD -> loadMusicAsset(event.value)
            AudioCommand.MUSIC_PLAY_ONCE -> playMusic(false, event.value)
            AudioCommand.MUSIC_PLAY_LOOP -> playMusic(true, event.value)
            AudioCommand.MUSIC_STOP -> {
                val music = _queuedMusic[event.value]
                music?.stop()
            }
            AudioCommand.MUSIC_STOP_ALL -> for (musicStop in _queuedMusic.values) {
                musicStop!!.stop()
            }
            AudioCommand.SOUND_LOAD -> loadSoundAsset(event.value)
            AudioCommand.SOUND_PLAY_LOOP -> playSound(true, event.value)
            AudioCommand.SOUND_PLAY_ONCE -> playSound(false, event.value)
            AudioCommand.SOUND_STOP -> {
                val sound = _queuedSounds[event.value]
                sound?.stop()
            }
            else -> {
            }
        }
    }

    private fun playMusic(isLooping: Boolean, fullFilePath: String?): Music? {
        var music = _queuedMusic[fullFilePath]
        if (music != null) {
            music.isLooping = isLooping
            music.play()
        } else if (isAssetLoaded(fullFilePath)) {
            music = getMusicAsset(fullFilePath!!)
            music!!.isLooping = isLooping
            music.play()
            _queuedMusic[fullFilePath] = music
        } else {
            Gdx.app.debug(TAG, "Music not loaded")
            return null
        }
        return music
    }

    private fun playSound(isLooping: Boolean, fullFilePath: String?): Sound? {
        var sound = _queuedSounds[fullFilePath]
        if (sound != null) {
            val soundId = sound.play()
            sound.setLooping(soundId, isLooping)
        } else if (isAssetLoaded(fullFilePath)) {
            sound = getSoundAsset(fullFilePath!!)
            val soundId = sound!!.play()
            sound.setLooping(soundId, isLooping)
            _queuedSounds[fullFilePath] = sound
        } else {
            Gdx.app.debug(TAG, "Sound not loaded")
            return null
        }
        return sound
    }

    fun dispose() {
        for (music in _queuedMusic.values) {
            music!!.dispose()
        }
        for (sound in _queuedSounds.values) {
            sound!!.dispose()
        }
    }

    companion object {
        private val TAG = AudioManager::class.java.simpleName
        private var _instance: AudioManager? = null
        @JvmStatic
        val instance: AudioManager
            get() {
                if (_instance == null) {
                    _instance = AudioManager()
                }
                return _instance!!
            }
    }

}