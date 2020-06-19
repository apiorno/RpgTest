package com.mygdx.game.battle

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Timer
import com.mygdx.game.ecs.Entity
import com.mygdx.game.ecs.EntityConfig
import com.mygdx.game.widgets.InventoryObserver
import com.mygdx.game.widgets.InventoryObserver.InventoryEvent
import com.mygdx.game.battle.BattleObserver.BattleEvent
import com.mygdx.game.profile.ProfileManager.Companion.instance

class BattleState : BattleSubject(), InventoryObserver {
    private var _currentOpponent: Entity? = null
    var currentZoneLevel = 0
    private var _currentPlayerAP = 0
    private var _currentPlayerDP = 0
    private var _currentPlayerWandAPPoints = 0
    private val _chanceOfAttack = 25
    private val _chanceOfEscape = 40
    private val _criticalChance = 90
    private val _playerAttackCalculations: Timer.Task
    private val _opponentAttackCalculations: Timer.Task
    private val _checkPlayerMagicUse: Timer.Task
    fun resetDefaults() {
        Gdx.app.debug(TAG, "Resetting defaults...")
        currentZoneLevel = 0
        _currentPlayerAP = 0
        _currentPlayerDP = 0
        _currentPlayerWandAPPoints = 0
        _playerAttackCalculations.cancel()
        _opponentAttackCalculations.cancel()
        _checkPlayerMagicUse.cancel()
    }

    //Gdx.app.debug(TAG, "CHANGE OF ATTACK: " + _chanceOfAttack + " randomval: " + randomVal);
    val isOpponentReady: Boolean
        get() {
            if (currentZoneLevel == 0) return false
            val randomVal = MathUtils.random(1, 100)

            //Gdx.app.debug(TAG, "CHANGE OF ATTACK: " + _chanceOfAttack + " randomval: " + randomVal);
            return if (_chanceOfAttack > randomVal) {
                setCurrentOpponent()
                true
            } else {
                false
            }
        }

    fun setCurrentOpponent() {
        Gdx.app.debug(TAG, "Entered BATTLE ZONE: " + currentZoneLevel)
        val entity: Entity = MonsterFactory.instance?.getRandomMonster(currentZoneLevel) ?: return
        _currentOpponent = entity
        notify(entity, BattleEvent.OPPONENT_ADDED)
    }

    fun playerAttacks() {
        if (_currentOpponent == null) {
            return
        }

        //Check for magic if used in attack; If we don't have enough MP, then return
        val mpVal = instance!!.getProperty("currentPlayerMP", Int::class.java)!!
        notify(_currentOpponent!!, BattleEvent.PLAYER_TURN_START)
        if (_currentPlayerWandAPPoints == 0) {
            if (!_playerAttackCalculations.isScheduled) {
                Timer.schedule(_playerAttackCalculations, 1f)
            }
        } else if (_currentPlayerWandAPPoints > mpVal) {
            notify(_currentOpponent!!, BattleEvent.PLAYER_TURN_DONE)
            return
        } else {
            if (!_checkPlayerMagicUse.isScheduled && !_playerAttackCalculations.isScheduled) {
                Timer.schedule(_checkPlayerMagicUse, .5f)
                Timer.schedule(_playerAttackCalculations, 1f)
            }
        }
    }

    fun opponentAttacks() {
        if (_currentOpponent == null) {
            return
        }
        if (!_opponentAttackCalculations.isScheduled) {
            Timer.schedule(_opponentAttackCalculations, 1f)
        }
    }

    private val playerMagicUseCheckTimer: Timer.Task
        private get() = object : Timer.Task() {
            override fun run() {
                var mpVal = instance!!.getProperty("currentPlayerMP", Int::class.java)!!
                mpVal -= _currentPlayerWandAPPoints
                instance!!.setProperty("currentPlayerMP", mpVal)
                notify(_currentOpponent!!, BattleEvent.PLAYER_USED_MAGIC)
            }
        }

    private val playerAttackCalculationTimer: Timer.Task
        private get() = object : Timer.Task() {
            override fun run() {
                var currentOpponentHP = _currentOpponent!!.entityConfig!!.getPropertyValue(EntityConfig.EntityProperties.ENTITY_HEALTH_POINTS.toString()).toInt()
                val currentOpponentDP = _currentOpponent!!.entityConfig!!.getPropertyValue(EntityConfig.EntityProperties.ENTITY_DEFENSE_POINTS.toString()).toInt()
                val damage = MathUtils.clamp(_currentPlayerAP - currentOpponentDP, 0, _currentPlayerAP)
                Gdx.app.debug(TAG, "ENEMY HAS $currentOpponentHP hit with damage: $damage")
                currentOpponentHP = MathUtils.clamp(currentOpponentHP - damage, 0, currentOpponentHP)
                _currentOpponent!!.entityConfig!!.setPropertyValue(EntityConfig.EntityProperties.ENTITY_HEALTH_POINTS.toString(), currentOpponentHP.toString())
                Gdx.app.debug(TAG, "Player attacks " + _currentOpponent!!.entityConfig!!.entityID + " leaving it with HP: " + currentOpponentHP)
                _currentOpponent!!.entityConfig!!.setPropertyValue(EntityConfig.EntityProperties.ENTITY_HIT_DAMAGE_TOTAL.toString(), damage.toString())
                if (damage > 0) {
                    notify(_currentOpponent!!, BattleEvent.OPPONENT_HIT_DAMAGE)
                }
                if (currentOpponentHP == 0) {
                    notify(_currentOpponent!!, BattleEvent.OPPONENT_DEFEATED)
                }
                notify(_currentOpponent!!, BattleEvent.PLAYER_TURN_DONE)
            }
        }

    private val opponentAttackCalculationTimer: Timer.Task
        private get() = object : Timer.Task() {
            override fun run() {
                val currentOpponentHP = _currentOpponent!!.entityConfig!!.getPropertyValue(EntityConfig.EntityProperties.ENTITY_HEALTH_POINTS.toString()).toInt()
                if (currentOpponentHP <= 0) {
                    notify(_currentOpponent!!, BattleEvent.OPPONENT_TURN_DONE)
                    return
                }
                val currentOpponentAP = _currentOpponent!!.entityConfig!!.getPropertyValue(EntityConfig.EntityProperties.ENTITY_ATTACK_POINTS.toString()).toInt()
                val damage = MathUtils.clamp(currentOpponentAP - _currentPlayerDP, 0, currentOpponentAP)
                var hpVal = instance!!.getProperty("currentPlayerHP", Int::class.java)!!
                hpVal = MathUtils.clamp(hpVal - damage, 0, hpVal)
                instance!!.setProperty("currentPlayerHP", hpVal)
                if (damage > 0) {
                    notify(_currentOpponent!!, BattleEvent.PLAYER_HIT_DAMAGE)
                }
                Gdx.app.debug(TAG, "Player HIT for " + damage + " BY " + _currentOpponent!!.entityConfig!!.entityID + " leaving player with HP: " + hpVal)
                notify(_currentOpponent!!, BattleEvent.OPPONENT_TURN_DONE)
            }
        }

    fun playerRuns() {
        val randomVal = MathUtils.random(1, 100)
        if (_chanceOfEscape > randomVal) {
            notify(_currentOpponent!!, BattleEvent.PLAYER_RUNNING)
        } else if (randomVal > _criticalChance) {
            opponentAttacks()
        } else {
            return
        }
    }

    override fun onNotify(value: String, event: InventoryEvent) {
        when (event) {
            InventoryEvent.UPDATED_AP -> {
                val apVal = Integer.valueOf(value)
                _currentPlayerAP = apVal
            }
            InventoryEvent.UPDATED_DP -> {
                val dpVal = Integer.valueOf(value)
                _currentPlayerDP = dpVal
            }
            InventoryEvent.ADD_WAND_AP -> {
                val wandAP = Integer.valueOf(value)
                _currentPlayerWandAPPoints += wandAP
                Gdx.app.debug(TAG, "WandAP: $_currentPlayerWandAPPoints")
            }
            InventoryEvent.REMOVE_WAND_AP -> {
                val removeWandAP = Integer.valueOf(value)
                _currentPlayerWandAPPoints -= removeWandAP
                Gdx.app.debug(TAG, "WandAP: $_currentPlayerWandAPPoints")
            }
            else -> {
            }
        }
    }

    companion object {
        private val TAG = BattleState::class.java.simpleName
    }

    init {
        _playerAttackCalculations = playerAttackCalculationTimer
        _opponentAttackCalculations = opponentAttackCalculationTimer
        _checkPlayerMagicUse = playerMagicUseCheckTimer
    }
}