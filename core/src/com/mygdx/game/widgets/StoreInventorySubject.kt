package com.mygdx.game.widgets

import com.mygdx.game.widgets.StoreInventoryObserver.StoreInventoryEvent

interface StoreInventorySubject {
    fun addObserver(storeObserver: StoreInventoryObserver)
    fun removeObserver(storeObserver: StoreInventoryObserver)
    fun removeAllObservers()
    fun notify(value: String, event: StoreInventoryEvent)
}