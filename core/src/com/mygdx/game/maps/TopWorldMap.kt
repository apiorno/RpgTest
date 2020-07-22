package com.mygdx.game.maps

import com.mygdx.game.maps.MapFactory.MapType
import com.mygdx.game.audio.AudioObserver.AudioCommand
import com.mygdx.game.audio.AudioObserver.AudioTypeEvent
import com.mygdx.game.sfx.ParticleEffectFactory
import com.mygdx.game.sfx.ParticleEffectFactory.ParticleEffectType

class TopWorldMap internal constructor() : Map(MapType.TOP_WORLD, mapPath) {

    override fun unloadMusic() {
        notify(AudioCommand.MUSIC_STOP, AudioTypeEvent.MUSIC_TOPWORLD)
    }

    override fun loadMusic() {
        notify(AudioCommand.MUSIC_LOAD, AudioTypeEvent.MUSIC_TOPWORLD)
        notify(AudioCommand.MUSIC_PLAY_LOOP, AudioTypeEvent.MUSIC_TOPWORLD)
    }

    companion object {
        private const val mapPath = "maps/topworld.tmx"
    }

    init {
        val lanternEffectPositions = getParticleEffectSpawnPositions(ParticleEffectType.LANTERN_FIRE)
        lanternEffectPositions.forEach { mapParticleEffects.add(ParticleEffectFactory.getParticleEffect(ParticleEffectType.LANTERN_FIRE, it)) }
        val lavaSmokeEffectPositions = getParticleEffectSpawnPositions(ParticleEffectType.LAVA_SMOKE)
        lavaSmokeEffectPositions.forEach { mapParticleEffects.add(ParticleEffectFactory.getParticleEffect(ParticleEffectType.LAVA_SMOKE, it)) }
    }
}