package com.mygdx.game.battle

import com.mygdx.game.ecs.Entity

interface BattleObserver {
    enum class BattleEvent {
        OPPONENT_ADDED, OPPONENT_HIT_DAMAGE, OPPONENT_DEFEATED, OPPONENT_TURN_DONE, PLAYER_HIT_DAMAGE, PLAYER_RUNNING, PLAYER_TURN_DONE, PLAYER_TURN_START, PLAYER_USED_MAGIC, NONE
    }

    fun onNotify(enemyEntity: Entity, event: BattleEvent)
}