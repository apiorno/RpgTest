package com.mygdx.game.temporal

interface StoreInventorySubject {
    fun addObserver(storeObserver: StoreInventoryObserver)
    fun removeObserver(storeObserver: StoreInventoryObserver)
    fun removeAllObservers()
    fun notify(value: String?, event: StoreInventoryObserver.StoreInventoryEvent?)
}