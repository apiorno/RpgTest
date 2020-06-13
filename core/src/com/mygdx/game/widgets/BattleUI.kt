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
import com.mygdx.game.sfx.ParticleEffectFactory

class BattleUI : Window("BATTLE", Utility.STATUSUI_SKIN, "solidbackground"), BattleObserver {
    private val _image: AnimatedImage
    private val _enemyWidth = 96
    private val _enemyHeight = 96
    var currentState: BattleState? = null
    private var _attackButton: TextButton? = null
    private var _runButton: TextButton? = null
    private var _damageValLabel: Label? = null
    private var _battleTimer = 0f
    private val _checkTimer = 1f
    private var _battleShakeCam: ShakeCamera? = null
    private val _effects: Array<ParticleEffect>
    private var _origDamageValLabelY = 0f
    private val _currentImagePosition: Vector2
    fun battleZoneTriggered(battleZoneValue: Int) {
        currentState!!.currentZoneLevel = battleZoneValue
    }

    val isBattleReady: Boolean
        get() = if (_battleTimer > _checkTimer) {
            _battleTimer = 0f
            currentState!!.isOpponentReady
        } else {
            false
        }

    override fun onNotify(entity: Entity?, event: BattleEvent?) {
        when (event) {
            BattleEvent.PLAYER_TURN_START -> {
                _runButton!!.isDisabled = true
                _runButton!!.touchable = Touchable.disabled
                _attackButton!!.isDisabled = true
                _attackButton!!.touchable = Touchable.disabled
            }
            BattleEvent.OPPONENT_ADDED -> {
                _image.setEntity(entity)
                _image.setCurrentAnimation(AnimationType.IMMOBILE)
                _image.setSize(_enemyWidth.toFloat(), _enemyHeight.toFloat())
                _image.setPosition(getCell(_image).actorX, getCell(_image).actorY)
                _currentImagePosition[_image.x] = _image.y
                if (_battleShakeCam == null) {
                    _battleShakeCam = ShakeCamera(_currentImagePosition.x, _currentImagePosition.y, 30.0f)
                }

                //Gdx.app.debug(TAG, "Image position: " + _image.getX() + "," + _image.getY() );
                titleLabel.setText("Level " + currentState!!.currentZoneLevel + " " + entity!!.entityConfig!!.entityID)
            }
            BattleEvent.OPPONENT_HIT_DAMAGE -> {
                val damage = entity!!.entityConfig!!.getPropertyValue(EntityConfig.EntityProperties.ENTITY_HIT_DAMAGE_TOTAL.toString()).toInt()
                _damageValLabel!!.setText(damage.toString())
                _damageValLabel!!.y = _origDamageValLabelY
                _battleShakeCam!!.startShaking()
                _damageValLabel!!.isVisible = true
            }
            BattleEvent.OPPONENT_DEFEATED -> {
                _damageValLabel!!.isVisible = false
                _damageValLabel!!.y = _origDamageValLabelY
            }
            BattleEvent.OPPONENT_TURN_DONE -> {
                _attackButton!!.isDisabled = false
                _attackButton!!.touchable = Touchable.enabled
                _runButton!!.isDisabled = false
                _runButton!!.touchable = Touchable.enabled
            }
            BattleEvent.PLAYER_TURN_DONE -> currentState!!.opponentAttacks()
            BattleEvent.PLAYER_USED_MAGIC -> {
                val x = _currentImagePosition.x + _enemyWidth / 2
                val y = _currentImagePosition.y + _enemyHeight / 2
                _effects.add(ParticleEffectFactory.getParticleEffect(ParticleEffectFactory.ParticleEffectType.WAND_ATTACK, x, y))
            }
            else -> {
            }
        }
    }

    fun resize() {
        _image.setPosition(getCell(_image).actorX, getCell(_image).actorY)
        _currentImagePosition[_image.x] = _image.y
        Gdx.app.debug(TAG, "RESIZE Image position: " + _image.x + "," + _image.y)
        if (_battleShakeCam != null) {
            _battleShakeCam!!.setOrigPosition(_currentImagePosition.x, _currentImagePosition.y)
            _battleShakeCam!!.reset()
        }
    }

    fun resetDefaults() {
        _battleTimer = 0f
        currentState!!.resetDefaults()
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        //Draw the particles last
        for (i in 0 until _effects.size) {
            val effect = _effects[i] ?: continue
            effect.draw(batch)
        }
    }

    override fun act(delta: Float) {
        _battleTimer = (_battleTimer + delta) % 60
        if (_damageValLabel!!.isVisible && _damageValLabel!!.y < height) {
            _damageValLabel!!.y = _damageValLabel!!.y + 5
        }
        if (_battleShakeCam != null && _battleShakeCam!!.isCameraShaking) {
            val shakeCoords = _battleShakeCam!!.newShakePosition
            _image.setPosition(shakeCoords.x, shakeCoords.y)
        }
        for (i in 0 until _effects.size) {
            val effect = _effects[i] ?: continue
            if (effect.isComplete) {
                _effects.removeIndex(i)
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
        _battleTimer = 0f
        currentState = BattleState()
        currentState.addObserver(this)
        _effects = Array()
        _currentImagePosition = Vector2(0, 0)
        _damageValLabel = Label("0", Utility.STATUSUI_SKIN)
        _damageValLabel.setVisible(false)
        _image = AnimatedImage()
        _image.touchable = Touchable.disabled
        val table = Table()
        _attackButton = TextButton("Attack", Utility.STATUSUI_SKIN, "inventory")
        _runButton = TextButton("Run", Utility.STATUSUI_SKIN, "inventory")
        table.add(_attackButton).pad(20f, 20f, 20f, 20f)
        table.row()
        table.add(_runButton).pad(20f, 20f, 20f, 20f)

        //layout
        setFillParent(true)
        this.add(_damageValLabel).align(Align.left).padLeft(_enemyWidth / 2.toFloat()).row()
        this.add(_image).size(_enemyWidth.toFloat(), _enemyHeight.toFloat()).pad(10f, 10f, 10f, _enemyWidth / 2.toFloat())
        this.add(table)
        pack()
        _origDamageValLabelY = _damageValLabel.getY() + _enemyHeight
        _attackButton.addListener(
                object : ClickListener() {
                    override fun clicked(event: InputEvent, x: Float, y: Float) {
                        currentState.playerAttacks()
                    }
                }
        )
        _runButton.addListener(
                object : ClickListener() {
                    override fun clicked(event: InputEvent, x: Float, y: Float) {
                        currentState.playerRuns()
                    }
                }
        )
    }
}