package com.mygdx.game.battle

import com.badlogic.gdx.utils.Array
import com.mygdx.game.ecs.Entity
import com.mygdx.game.battle.BattleObserver.BattleEvent

open class BattleSubject {
    private val _observers: Array<BattleObserver>
    fun addObserver(battleObserver: BattleObserver) {
        _observers.add(battleObserver)
    }

    fun removeObserver(battleObserver: BattleObserver) {
        _observers.removeValue(battleObserver, true)
    }

    protected fun notify(entity: Entity, event: BattleEvent) {
        for (observer in _observers) {
            observer.onNotify(entity, event)
        }
    }

    init {
        _observers = Array()
    }
}