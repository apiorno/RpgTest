package com.mygdx.game.sfx

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.math.Vector2

object ParticleEffectFactory {
    private const val SFX_ROOT_DIR = "sfx"

    fun getParticleEffect(particleEffectType: ParticleEffectType, positionX: Float, positionY: Float): ParticleEffect {
        val effect = ParticleEffect()
        effect.load(Gdx.files.internal(particleEffectType.value), Gdx.files.internal(SFX_ROOT_DIR))
        effect.setPosition(positionX, positionY)
        when (particleEffectType) {
            ParticleEffectType.CANDLE_FIRE -> effect.scaleEffect(.04f)
            ParticleEffectType.LANTERN_FIRE -> effect.scaleEffect(.02f)
            ParticleEffectType.LAVA_SMOKE -> effect.scaleEffect(.04f)
            ParticleEffectType.WAND_ATTACK -> effect.scaleEffect(1.0f)
            else -> {
            }
        }
        effect.start()
        return effect
    }

    fun getParticleEffect(particleEffectType: ParticleEffectType, position: Vector2): ParticleEffect {
        return getParticleEffect(particleEffectType, position.x, position.y)
    }

    enum class ParticleEffectType(val value: String) {
        CANDLE_FIRE("sfx/candle.p"), LANTERN_FIRE("sfx/candle.p"), LAVA_SMOKE("sfx/smoke.p"), WAND_ATTACK("sfx/magic_attack.p"), NONE("");

    }
}