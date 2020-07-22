package com.mygdx.game.maps

import com.mygdx.game.maps.MapFactory.MapType
import com.mygdx.game.audio.AudioObserver.AudioCommand
import com.mygdx.game.audio.AudioObserver.AudioTypeEvent
import com.mygdx.game.sfx.ParticleEffectFactory
import com.mygdx.game.sfx.ParticleEffectFactory.ParticleEffectType

class CastleDoomMap internal constructor() : Map(MapType.CASTLE_OF_DOOM, mapPath) {
    override fun unloadMusic() {
        notify(AudioCommand.MUSIC_STOP, AudioTypeEvent.MUSIC_CASTLEDOOM)
    }

    override fun loadMusic() {
        notify(AudioCommand.MUSIC_LOAD, AudioTypeEvent.MUSIC_CASTLEDOOM)
        notify(AudioCommand.MUSIC_PLAY_LOOP, AudioTypeEvent.MUSIC_CASTLEDOOM)
    }

    companion object {
        private const val mapPath = "maps/castle_of_doom.tmx"
    }

    init {
        val candleEffectPositions = getParticleEffectSpawnPositions(ParticleEffectType.CANDLE_FIRE)
        candleEffectPositions.forEach { mapParticleEffects.add(ParticleEffectFactory.getParticleEffect(ParticleEffectType.CANDLE_FIRE, it)) }
        val lavaSmokeEffectPositions = getParticleEffectSpawnPositions(ParticleEffectType.LAVA_SMOKE)
        lavaSmokeEffectPositions.forEach { mapParticleEffects.add(ParticleEffectFactory.getParticleEffect(ParticleEffectType.LAVA_SMOKE, it)) }
    }
}