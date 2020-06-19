package com.mygdx.game.widgets

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.mygdx.game.widgets.InventorySlotObserver.SlotEvent
import com.mygdx.game.Utility

class InventorySlot() : Stack(), InventorySlotSubject {
    //All slots have this default image
    private val _defaultBackground: Stack
    private var _customBackgroundDecal: Image
    private val _numItemsLabel: Label?
    private var _numItemsVal = 0
    private var _filterItemType = 0
    private val _observers: Array<InventorySlotObserver>

    constructor(filterItemType: Int, customBackgroundDecal: Image) : this() {
        _filterItemType = filterItemType
        _customBackgroundDecal = customBackgroundDecal
        _defaultBackground.add(_customBackgroundDecal)
    }

    fun decrementItemCount(sendRemoveNotification: Boolean) {
        _numItemsVal--
        _numItemsLabel!!.setText(_numItemsVal.toString())
        if (_defaultBackground.children.size == 1) {
            _defaultBackground.add(_customBackgroundDecal)
        }
        checkVisibilityOfItemCount()
        if (sendRemoveNotification) {
            notify(this, SlotEvent.REMOVED_ITEM)
        }
    }

    fun incrementItemCount(sendAddNotification: Boolean) {
        _numItemsVal++
        _numItemsLabel!!.setText(_numItemsVal.toString())
        if (_defaultBackground.children.size > 1) {
            _defaultBackground.children.pop()
        }
        checkVisibilityOfItemCount()
        if (sendAddNotification) {
            notify(this, SlotEvent.ADDED_ITEM)
        }
    }

    override fun add(actor: Actor) {
        super.add(actor)
        if (_numItemsLabel == null) {
            return
        }
        if (actor != _defaultBackground && actor != _numItemsLabel) {
            incrementItemCount(true)
        }
    }

    fun remove(actor: Actor?) {
        super.removeActor(actor)
        if (_numItemsLabel == null) {
            return
        }
        if (actor != _defaultBackground && actor != _numItemsLabel) {
            decrementItemCount(true)
        }
    }

    fun add(array: Array<Actor>) {
        for (actor in array) {
            super.add(actor)
            if (_numItemsLabel == null) {
                return
            }
            if (actor != _defaultBackground && actor != _numItemsLabel) {
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
        if (_numItemsVal < 2) {
            _numItemsLabel!!.isVisible = false
        } else {
            _numItemsLabel!!.isVisible = true
        }
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
        return if (_filterItemType == 0) {
            true
        } else {
            _filterItemType and itemUseType == itemUseType
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
        _observers.add(inventorySlotObserver)
    }

    override fun removeObserver(inventorySlotObserver: InventorySlotObserver) {
        _observers.removeValue(inventorySlotObserver, true)
    }

    override fun removeAllObservers() {
        for (observer in _observers) {
            _observers.removeValue(observer, true)
        }
    }

    override fun notify(slot: InventorySlot, event: SlotEvent) {
        for (observer in _observers) {
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
        _defaultBackground = Stack()
        _customBackgroundDecal = Image()
        _observers = Array()
        val image = Image(NinePatch(Utility.STATUSUI_TEXTUREATLAS.createPatch("dialog")))
        _numItemsLabel = Label(_numItemsVal.toString(), Utility.STATUSUI_SKIN, "inventory-item-count")
        _numItemsLabel.setAlignment(Align.bottomRight)
        _numItemsLabel.isVisible = false
        _defaultBackground.add(image)
        _defaultBackground.name = "background"
        _numItemsLabel.name = "numitems"
        this.add(_defaultBackground)
        this.add(_numItemsLabel)
    }
}