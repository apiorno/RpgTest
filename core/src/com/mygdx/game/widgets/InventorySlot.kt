package com.mygdx.game.widgets

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.mygdx.game.Utility
import com.mygdx.game.temporal.InventorySlotObserver
import com.mygdx.game.temporal.InventorySlotObserver.*
import com.mygdx.game.temporal.InventorySlotSubject


class InventorySlot() : Stack(), InventorySlotSubject {
    //All slots have this default image
    private val defaultBackground: Stack = Stack()
    private var customBackgroundDecal: Image
    private val numItemsLabel: Label?
    private var numItemsVal = 0
    private var filterItemType = 0
    private val observers: Array<InventorySlotObserver>

    constructor(filterItemType: Int, customBackgroundDecal: Image) : this() {
        this.filterItemType = filterItemType
        this.customBackgroundDecal = customBackgroundDecal
        defaultBackground.add(customBackgroundDecal)
    }

    fun decrementItemCount(sendRemoveNotification: Boolean) {
        numItemsVal.dec()
        numItemsLabel!!.setText(numItemsVal.toString())
        if (defaultBackground.children.size == 1) {
            defaultBackground.add(customBackgroundDecal)
        }
        checkVisibilityOfItemCount()
        if (sendRemoveNotification) {
            notify(this, SlotEvent.REMOVED_ITEM)
        }
    }

    private fun incrementItemCount(sendAddNotification: Boolean) {
        numItemsVal++
        numItemsLabel!!.setText(numItemsVal.toString())
        if (defaultBackground.children.size > 1) {
            defaultBackground.children.pop()
        }
        checkVisibilityOfItemCount()
        if (sendAddNotification) {
            notify(this, SlotEvent.ADDED_ITEM)
        }
    }

    override fun add(actor: Actor) {
        super.add(actor)
        if (numItemsLabel == null) {
            return
        }
        if (actor != defaultBackground && actor != numItemsLabel) {
            incrementItemCount(true)
        }
    }

    fun remove(actor: Actor?) {
        super.removeActor(actor)
        if (numItemsLabel == null) {
            return
        }
        if (actor != defaultBackground && actor != numItemsLabel) {
            decrementItemCount(true)
        }
    }

    fun add(array: Array<Actor>) {
        for (actor in array) {
            super.add(actor)
            if (numItemsLabel == null) {
                return
            }
            if (actor != defaultBackground && actor != numItemsLabel) {
                incrementItemCount(true)
            }
        }
    }

    val allInventoryItems: Array<Actor>
        get() {
            val items = Array<Actor>()
            if (hasItem()) {
                val arrayChildren = children
                val numInventoryItems = arrayChildren.size - 2
                for (i in 0 until numInventoryItems) {
                    decrementItemCount(true)
                    items.add(arrayChildren.pop())
                }
            }
            return items
        }

    fun updateAllInventoryItemNames(name: String?) {
        if (hasItem()) {
            val arrayChildren = children
            //skip first two elements
            for (i in arrayChildren.size - 1 downTo 2) {
                arrayChildren[i].name = name
            }
        }
    }

    fun removeAllInventoryItemsWithName(name: String?) {
        if (hasItem()) {
            val arrayChildren = children
            //skip first two elements
            for (i in arrayChildren.size - 1 downTo 2) {
                val itemName = arrayChildren[i].name
                if (itemName.equals(name, ignoreCase = true)) {
                    decrementItemCount(true)
                    arrayChildren.removeIndex(i)
                }
            }
        }
    }

    fun clearAllInventoryItems(sendRemoveNotifications: Boolean) {
        if (hasItem()) {
            val arrayChildren = children
            val numInventoryItems = numItems
            for (i in 0 until numInventoryItems) {
                decrementItemCount(sendRemoveNotifications)
                arrayChildren.pop()
            }
        }
    }

    private fun checkVisibilityOfItemCount() {
        numItemsLabel!!.isVisible = numItemsVal >= 2
    }

    fun hasItem(): Boolean {
        if (hasChildren()) {
            val items = children
            if (items.size > 2) {
                return true
            }
        }
        return false
    }

    val numItems: Int
        get() {
            if (hasChildren()) {
                val items = children
                return items.size - 2
            }
            return 0
        }

    fun getNumItems(name: String?): Int {
        if (hasChildren()) {
            val items = children
            var totalFilteredSize = 0
            for (actor in items) {
                if (actor.name.equals(name, ignoreCase = true)) {
                    totalFilteredSize++
                }
            }
            return totalFilteredSize
        }
        return 0
    }

    fun doesAcceptItemUseType(itemUseType: Int): Boolean {
        return if (filterItemType == 0) {
            true
        } else {
            filterItemType and itemUseType == itemUseType
        }
    }

    val topInventoryItem: InventoryItem?
        get() {
            var actor: InventoryItem? = null
            if (hasChildren()) {
                val items = children
                if (items.size > 2) {
                    actor = items.peek() as InventoryItem
                }
            }
            return actor
        }

    override fun addObserver(inventorySlotObserver: InventorySlotObserver) {
        observers.add(inventorySlotObserver)
    }

    override fun removeObserver(inventorySlotObserver: InventorySlotObserver) {
        observers.removeValue(inventorySlotObserver, true)
    }

    override fun removeAllObservers() {
        for (observer in observers) {
            observers.removeValue(observer, true)
        }
    }

    override fun notify(slot: InventorySlot, event: SlotEvent?) {
        for (observer in observers) {
            observer.onNotify(slot, event)
        }
    }

    companion object {
        fun swapSlots(inventorySlotSource: InventorySlot?, inventorySlotTarget: InventorySlot, dragActor: InventoryItem) {
            //check if items can accept each other, otherwise, no swap
            if (!inventorySlotTarget.doesAcceptItemUseType(dragActor.itemUseType) ||
                    !inventorySlotSource!!.doesAcceptItemUseType(inventorySlotTarget.topInventoryItem!!.itemUseType)) {
                inventorySlotSource!!.add(dragActor)
                return
            }

            //swap
            val tempArray = inventorySlotSource.allInventoryItems
            tempArray.add(dragActor)
            inventorySlotSource.add(inventorySlotTarget.allInventoryItems)
            inventorySlotTarget.add(tempArray)
        }
    }

    init {
        //filter nothing
        customBackgroundDecal = Image()
        observers = Array()
        val image = Image(NinePatch(Utility.STATUSUI_TEXTUREATLAS.createPatch("dialog")))
        numItemsLabel = Label(numItemsVal.toString(), Utility.STATUSUI_SKIN, "inventory-item-count")
        numItemsLabel.setAlignment(Align.bottomRight)
        numItemsLabel.isVisible = false
        defaultBackground.add(image)
        defaultBackground.name = "background"
        numItemsLabel.name = "numitems"
        this.add(defaultBackground)
        this.add(numItemsLabel)
    }
}