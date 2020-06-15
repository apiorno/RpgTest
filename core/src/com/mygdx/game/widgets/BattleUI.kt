package com.mygdx.game.widgets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.mygdx.game.EntityConfig
import com.mygdx.game.Utility
import com.mygdx.game.battle.BattleState
import com.mygdx.game.sfx.ParticleEffectFactory
import com.mygdx.game.sfx.ShakeCamera
import com.mygdx.game.temporal.BattleObserver
import com.mygdx.game.temporal.BattleObserver.*

class BattleUI : Window("BATTLE", Utility.STATUSUI_SKIN, "solidbackground"), BattleObserver {
    private val image: AnimatedImage
    private val enemyWidth = 96
    private val enemyHeight = 96
    var currentState: BattleState? = null
    private var attackButton: TextButton? = null
    private var runButton: TextButton? = null
    private var damageValLabel: Label? = null
    private var battleTimer = 0f
    private val checkTimer = 1f
    private var battleShakeCam: ShakeCamera? = null
    private val effects: Array<ParticleEffect>
    private var origDamageValLabelY = 0f
    private val currentImagePosition: Vector2
    fun battleZoneTriggered(battleZoneValue: Int) {
        currentState!!.currentZoneLevel = battleZoneValue
    }

    val isBattleReady: Boolean
        get() = if (battleTimer > checkTimer) {
            battleTimer = 0f
            currentState!!.isOpponentReady
        } else {
            false
        }

    override fun onNotify(enemyEntityConfig: EntityConfig?, event: BattleEvent?) {
        when (event) {
            BattleEvent.PLAYER_TURN_START -> {
                runButton!!.isDisabled = true
                runButton!!.touchable = Touchable.disabled
                attackButton!!.isDisabled = true
                attackButton!!.touchable = Touchable.disabled
            }
            BattleEvent.OPPONENT_ADDED -> {
                image.setEntityConfig(enemyEntityConfig!!)
                image.setCurrentAnimation(AnimationType.IMMOBILE)
                image.setSize(enemyWidth.toFloat(), enemyHeight.toFloat())
                image.setPosition(getCell(image).actorX, getCell(image).actorY)
                currentImagePosition[image.x] = image.y
                if (battleShakeCam == null) {
                    battleShakeCam = ShakeCamera(currentImagePosition.x, currentImagePosition.y, 30.0f)
                }

                //Gdx.app.debug(TAG, "Image position: " + image.getX() + "," + image.getY() );
                titleLabel.setText("Level " + currentState!!.currentZoneLevel + " " + enemyEntityConfig.entityID)
            }
            BattleEvent.OPPONENT_HIT_DAMAGE -> {
                val damage = enemyEntityConfig!!.getPropertyValue(EntityConfig.EntityProperties.ENTITY_HIT_DAMAGE_TOTAL.toString()).toInt()
                damageValLabel!!.setText(damage.toString())
                damageValLabel!!.y = origDamageValLabelY
                battleShakeCam!!.startShaking()
                damageValLabel!!.isVisible = true
            }
            BattleEvent.OPPONENT_DEFEATED -> {
                damageValLabel!!.isVisible = false
                damageValLabel!!.y = origDamageValLabelY
            }
            BattleEvent.OPPONENT_TURN_DONE -> {
                attackButton!!.isDisabled = false
                attackButton!!.touchable = Touchable.enabled
                runButton!!.isDisabled = false
                runButton!!.touchable = Touchable.enabled
            }
            BattleEvent.PLAYER_TURN_DONE -> currentState!!.opponentAttacks()
            BattleEvent.PLAYER_USED_MAGIC -> {
                val x = currentImagePosition.x + enemyWidth / 2
                val y = currentImagePosition.y + enemyHeight / 2
                effects.add(ParticleEffectFactory.getParticleEffect(ParticleEffectFactory.ParticleEffectType.WAND_ATTACK, x, y))
            }
            else -> {
            }
        }
    }

    fun resize() {
        image.setPosition(getCell(image).actorX, getCell(image).actorY)
        currentImagePosition[image.x] = image.y
        Gdx.app.debug(TAG, "RESIZE Image position: " + image.x + "," + image.y)
        if (battleShakeCam != null) {
            battleShakeCam!!.setOrigPosition(currentImagePosition.x, currentImagePosition.y)
            battleShakeCam!!.reset()
        }
    }

    fun resetDefaults() {
        battleTimer = 0f
        currentState!!.resetDefaults()
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        //Draw the particles last
        for (i in 0 until effects.size) {
            val effect = effects[i] ?: continue
            effect.draw(batch)
        }
    }

    override fun act(delta: Float) {
        battleTimer = (battleTimer + delta) % 60
        if (damageValLabel!!.isVisible && damageValLabel!!.y < height) {
            damageValLabel!!.y = damageValLabel!!.y + 5
        }
        if (battleShakeCam != null && battleShakeCam!!.isCameraShaking) {
            val shakeCoords = battleShakeCam!!.newShakePosition
            image.setPosition(shakeCoords.x, shakeCoords.y)
        }
        for (i in 0 until effects.size) {
            val effect = effects[i] ?: continue
            if (effect.isComplete) {
                effects.removeIndex(i)
                effect.dispose()
            } else {
                effect.update(delta)
            }
        }
        super.act(delta)
    }

    companion object {
        private val TAG = BattleUI::class.java.simpleName
    }

    init {
        battleTimer = 0f
        currentState = BattleState()
        currentState!!.addObserver(this)
        effects = Array()
        currentImagePosition = Vector2(0f, 0f)
        damageValLabel = Label("0", Utility.STATUSUI_SKIN)
        damageValLabel!!.isVisible = false
        image = AnimatedImage()
        image.touchable = Touchable.disabled
        val table = Table()
        attackButton = TextButton("Attack", Utility.STATUSUI_SKIN, "inventory")
        runButton = TextButton("Run", Utility.STATUSUI_SKIN, "inventory")
        table.add(attackButton).pad(20f, 20f, 20f, 20f)
        table.row()
        table.add(runButton).pad(20f, 20f, 20f, 20f)

        //layout
        setFillParent(true)
        this.add(damageValLabel).align(Align.left).padLeft(enemyWidth / 2.toFloat()).row()
        this.add(image).size(enemyWidth.toFloat(), enemyHeight.toFloat()).pad(10f, 10f, 10f, enemyWidth / 2.toFloat())
        this.add(table)
        pack()
        origDamageValLabelY = damageValLabel!!.y + enemyHeight
        attackButton!!.addListener(
                object : ClickListener() {
                    override fun clicked(event: InputEvent, x: Float, y: Float) {
                        currentState!!.playerAttacks()
                    }
                }
        )
        runButton!!.addListener(
                object : ClickListener() {
                    override fun clicked(event: InputEvent, x: Float, y: Float) {
                        currentState!!.playerRuns()
                    }
                }
        )
    }
}