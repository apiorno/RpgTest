package com.mygdx.game.maps

import com.mygdx.game.maps.MapFactory.MapType
import com.mygdx.game.audio.AudioObserver.AudioCommand
import com.mygdx.game.audio.AudioObserver.AudioTypeEvent
import com.mygdx.game.sfx.ParticleEffectFactory
import com.mygdx.game.sfx.ParticleEffectFactory.ParticleEffectType

class CastleDoomMap internal constructor() : Map(MapType.CASTLE_OF_DOOM, _mapPath) {
    override fun unloadMusic() {
        notify(AudioCommand.MUSIC_STOP, AudioTypeEvent.MUSIC_CASTLEDOOM)
    }

    override fun loadMusic() {
        notify(AudioCommand.MUSIC_LOAD, AudioTypeEvent.MUSIC_CASTLEDOOM)
        notify(AudioCommand.MUSIC_PLAY_LOOP, AudioTypeEvent.MUSIC_CASTLEDOOM)
    }

    companion object {
        private const val _mapPath = "maps/castle_of_doom.tmx"
    }

    init {
        val candleEffectPositions = getParticleEffectSpawnPositions(ParticleEffectType.CANDLE_FIRE)
        for (position in candleEffectPositions) {
            mapParticleEffects.add(ParticleEffectFactory.getParticleEffect(ParticleEffectType.CANDLE_FIRE, position))
        }
        val lavaSmokeEffectPositions = getParticleEffectSpawnPositions(ParticleEffectType.LAVA_SMOKE)
        for (position in lavaSmokeEffectPositions) {
            mapParticleEffects.add(ParticleEffectFactory.getParticleEffect(ParticleEffectType.LAVA_SMOKE, position))
        }
    }
}