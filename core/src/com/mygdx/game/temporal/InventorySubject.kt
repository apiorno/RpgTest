package com.mygdx.game.temporal

interface InventorySubject {
    fun addObserver(inventoryObserver: InventoryObserver?)
    fun removeObserver(inventoryObserver: InventoryObserver?)
    fun removeAllObservers()
    fun notify(value: String, event: InventoryObserver.InventoryEvent?)
}