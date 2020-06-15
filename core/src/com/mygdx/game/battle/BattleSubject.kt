package com.mygdx.game.battle

import com.badlogic.gdx.utils.Array
import com.mygdx.game.EntityConfig
import com.mygdx.game.temporal.BattleObserver
import com.mygdx.game.temporal.BattleObserver.*

open class BattleSubject {
    private val observers: Array<BattleObserver> = Array()
    fun addObserver(battleObserver: BattleObserver) {
        observers.add(battleObserver)
    }

    fun removeObserver(battleObserver: BattleObserver) {
        observers.removeValue(battleObserver, true)
    }

    protected fun notify(entityConfig: EntityConfig?, event: BattleEvent?) {
        for (observer in observers) {
            observer.onNotify(entityConfig, event)
        }
    }

}