package com.mygdx.game.temporal

import com.mygdx.game.widgets.InventorySlot
import com.mygdx.game.temporal.InventorySlotObserver.*

interface InventorySlotSubject {
    fun addObserver(inventorySlotObserver: InventorySlotObserver)
    fun removeObserver(inventorySlotObserver: InventorySlotObserver)
    fun removeAllObservers()
    fun notify(slot: InventorySlot, event: SlotEvent?)
}