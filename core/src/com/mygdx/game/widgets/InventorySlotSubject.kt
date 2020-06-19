package com.mygdx.game.widgets

import com.mygdx.game.widgets.InventorySlotObserver.SlotEvent

interface InventorySlotSubject {
    fun addObserver(inventorySlotObserver: InventorySlotObserver)
    fun removeObserver(inventorySlotObserver: InventorySlotObserver)
    fun removeAllObservers()
    fun notify(slot: InventorySlot, event: SlotEvent)
}