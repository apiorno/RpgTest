package com.mygdx.game.temporal

import com.mygdx.game.widgets.InventorySlot

interface InventorySlotObserver {
    enum class SlotEvent {
        ADDED_ITEM, REMOVED_ITEM
    }

    fun onNotify(slot: InventorySlot, event: SlotEvent?)
}