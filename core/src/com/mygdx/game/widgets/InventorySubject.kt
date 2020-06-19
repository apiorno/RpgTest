package com.mygdx.game.widgets

import com.mygdx.game.widgets.InventoryObserver.InventoryEvent

interface InventorySubject {
    fun addObserver(inventoryObserver: InventoryObserver)
    fun removeObserver(inventoryObserver: InventoryObserver)
    fun removeAllObservers()
    fun notify(value: String, event: InventoryEvent)
}