package com.mygdx.game.battle

import com.badlogic.gdx.utils.Array
import com.mygdx.game.ecs.Entity
import com.mygdx.game.battle.BattleObserver.BattleEvent

open class BattleSubject {
    private val observers: Array<BattleObserver> = Array()
    fun addObserver(battleObserver: BattleObserver) {
        observers.add(battleObserver)
    }

    fun removeObserver(battleObserver: BattleObserver) {
        observers.removeValue(battleObserver, true)
    }

    protected fun notify(entity: Entity, event: BattleEvent) {
        observers.forEach { it.onNotify(entity, event) }
    }
}