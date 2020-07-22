package com.mygdx.game.battle

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Timer
import com.mygdx.game.ecs.Entity
import com.mygdx.game.ecs.EntityConfig
import com.mygdx.game.widgets.InventoryObserver
import com.mygdx.game.widgets.InventoryObserver.InventoryEvent
import com.mygdx.game.battle.BattleObserver.BattleEvent
import com.mygdx.game.profile.ProfileManager

class BattleState : BattleSubject(), InventoryObserver {
    private var currentOpponent: Entity? = null
    var currentZoneLevel = 0
    private var currentPlayerAP = 0
    private var currentPlayerDP = 0
    private var currentPlayerWandAPPoints = 0
    private val chanceOfAttack = 25
    private val chanceOfEscape = 40
    private val criticalChance = 90
    private val playerAttackCalculations: Timer.Task
    private val opponentAttackCalculations: Timer.Task
    private val checkPlayerMagicUse: Timer.Task

    fun resetDefaults() {
        Gdx.app.debug(TAG, "Resetting defaults...")
        currentZoneLevel = 0
        currentPlayerAP = 0
        currentPlayerDP = 0
        currentPlayerWandAPPoints = 0
        playerAttackCalculations.cancel()
        opponentAttackCalculations.cancel()
        checkPlayerMagicUse.cancel()
    }

    //Gdx.app.debug(TAG, "CHANGE OF ATTACK: " + _chanceOfAttack + " randomval: " + randomVal);
    val isOpponentReady: Boolean
        get() {
            if (currentZoneLevel == 0) return false
            val randomVal = MathUtils.random(1, 100)

            //Gdx.app.debug(TAG, "CHANGE OF ATTACK: " + _chanceOfAttack + " randomval: " + randomVal);
            val ready = chanceOfAttack > randomVal
            if (ready) setCurrentOpponent()

            return ready
        }

    private fun setCurrentOpponent() {
        Gdx.app.debug(TAG, "Entered BATTLE ZONE: $currentZoneLevel")
        val entity: Entity = MonsterFactory.getRandomMonster(currentZoneLevel) ?: return
        currentOpponent = entity
        notify(entity, BattleEvent.OPPONENT_ADDED)
    }

    fun playerAttacks() {
        if (currentOpponent == null) {
            return
        }

        //Check for magic if used in attack; If we don't have enough MP, then return
        val mpVal = ProfileManager.getProperty("currentPlayerMP", Int::class.java)!!
        notify(currentOpponent!!, BattleEvent.PLAYER_TURN_START)
        if (currentPlayerWandAPPoints == 0 && !playerAttackCalculations.isScheduled) {
            Timer.schedule(playerAttackCalculations, 1f)
        } else if (currentPlayerWandAPPoints > mpVal) {
            notify(currentOpponent!!, BattleEvent.PLAYER_TURN_DONE)
            return
        } else if (!checkPlayerMagicUse.isScheduled && !playerAttackCalculations.isScheduled) {
                Timer.schedule(checkPlayerMagicUse, .5f)
                Timer.schedule(playerAttackCalculations, 1f)
            }
    }

    fun opponentAttacks() {
        if (currentOpponent == null) {
            return
        }
        if (!opponentAttackCalculations.isScheduled) {
            Timer.schedule(opponentAttackCalculations, 1f)
        }
    }

    private val playerMagicUseCheckTimer: Timer.Task
        get() = object : Timer.Task() {
            override fun run() {
                var mpVal = ProfileManager.getProperty("currentPlayerMP", Int::class.java)!!
                mpVal -= currentPlayerWandAPPoints
                ProfileManager.setProperty("currentPlayerMP", mpVal)
                notify(currentOpponent!!, BattleEvent.PLAYER_USED_MAGIC)
            }
        }

    private val playerAttackCalculationTimer: Timer.Task
        get() = object : Timer.Task() {
            override fun run() {
                var currentOpponentHP = currentOpponent!!.entityConfig!!.getPropertyValue(EntityConfig.EntityProperties.ENTITY_HEALTH_POINTS.toString()).toInt()
                val currentOpponentDP = currentOpponent!!.entityConfig!!.getPropertyValue(EntityConfig.EntityProperties.ENTITY_DEFENSE_POINTS.toString()).toInt()
                val damage = MathUtils.clamp(currentPlayerAP - currentOpponentDP, 0, currentPlayerAP)
                Gdx.app.debug(TAG, "ENEMY HAS $currentOpponentHP hit with damage: $damage")
                currentOpponentHP = MathUtils.clamp(currentOpponentHP - damage, 0, currentOpponentHP)
                currentOpponent!!.entityConfig!!.setPropertyValue(EntityConfig.EntityProperties.ENTITY_HEALTH_POINTS.toString(), currentOpponentHP.toString())
                Gdx.app.debug(TAG, "Player attacks " + currentOpponent!!.entityConfig!!.entityID + " leaving it with HP: " + currentOpponentHP)
                currentOpponent!!.entityConfig!!.setPropertyValue(EntityConfig.EntityProperties.ENTITY_HIT_DAMAGE_TOTAL.toString(), damage.toString())
                if (damage > 0) {
                    notify(currentOpponent!!, BattleEvent.OPPONENT_HIT_DAMAGE)
                }
                if (currentOpponentHP == 0) {
                    notify(currentOpponent!!, BattleEvent.OPPONENT_DEFEATED)
                }
                notify(currentOpponent!!, BattleEvent.PLAYER_TURN_DONE)
            }
        }

    private val opponentAttackCalculationTimer: Timer.Task
        get() = object : Timer.Task() {
            override fun run() {
                val currentOpponentHP = currentOpponent!!.entityConfig!!.getPropertyValue(EntityConfig.EntityProperties.ENTITY_HEALTH_POINTS.toString()).toInt()
                if (currentOpponentHP <= 0) {
                    notify(currentOpponent!!, BattleEvent.OPPONENT_TURN_DONE)
                    return
                }
                val currentOpponentAP = currentOpponent!!.entityConfig!!.getPropertyValue(EntityConfig.EntityProperties.ENTITY_ATTACK_POINTS.toString()).toInt()
                val damage = MathUtils.clamp(currentOpponentAP - currentPlayerDP, 0, currentOpponentAP)
                var hpVal = ProfileManager.getProperty("currentPlayerHP", Int::class.java)!!
                hpVal = MathUtils.clamp(hpVal - damage, 0, hpVal)
                ProfileManager.setProperty("currentPlayerHP", hpVal)
                if (damage > 0) {
                    notify(currentOpponent!!, BattleEvent.PLAYER_HIT_DAMAGE)
                }
                Gdx.app.debug(TAG, "Player HIT for " + damage + " BY " + currentOpponent!!.entityConfig!!.entityID + " leaving player with HP: " + hpVal)
                notify(currentOpponent!!, BattleEvent.OPPONENT_TURN_DONE)
            }
        }

    fun playerRuns() {
        val randomVal = MathUtils.random(1, 100)
        if (chanceOfEscape > randomVal) {
            notify(currentOpponent!!, BattleEvent.PLAYER_RUNNING)
        } else if (randomVal > criticalChance) {
            opponentAttacks()
        } else {
            return
        }
    }

    override fun onNotify(value: String, event: InventoryEvent) {
        when (event) {
            InventoryEvent.UPDATED_AP -> {
                val apVal = Integer.valueOf(value)
                currentPlayerAP = apVal
            }
            InventoryEvent.UPDATED_DP -> {
                val dpVal = Integer.valueOf(value)
                currentPlayerDP = dpVal
            }
            InventoryEvent.ADD_WAND_AP -> {
                val wandAP = Integer.valueOf(value)
                currentPlayerWandAPPoints += wandAP
                Gdx.app.debug(TAG, "WandAP: $currentPlayerWandAPPoints")
            }
            InventoryEvent.REMOVE_WAND_AP -> {
                val removeWandAP = Integer.valueOf(value)
                currentPlayerWandAPPoints -= removeWandAP
                Gdx.app.debug(TAG, "WandAP: $currentPlayerWandAPPoints")
            }
            else -> {
            }
        }
    }

    companion object {
        private val TAG = BattleState::class.java.simpleName
    }

    init {
        playerAttackCalculations = playerAttackCalculationTimer
        opponentAttackCalculations = opponentAttackCalculationTimer
        checkPlayerMagicUse = playerMagicUseCheckTimer
    }
}