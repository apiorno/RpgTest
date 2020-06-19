package com.mygdx.game.widgets

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.mygdx.game.ecs.Component
import com.mygdx.game.ecs.Entity
import com.mygdx.game.widgets.InventoryItem.ItemTypeID
import com.mygdx.game.widgets.InventoryItem.ItemUseType
import com.mygdx.game.widgets.InventoryObserver.InventoryEvent
import com.mygdx.game.widgets.InventorySlotObserver.SlotEvent
import com.mygdx.game.Utility

class InventoryUI : Window("Inventory", Utility.STATUSUI_SKIN, "solidbackground"), InventorySubject, InventorySlotObserver {
    private val _lengthSlotRow = 10
    val inventorySlotTable: Table
    private val _playerSlotsTable: Table
    val equipSlotTable: Table
    val dragAndDrop: DragAndDrop
    val inventoryActors: Array<Actor>
    private val _DPValLabel: Label
    private var _DPVal = 0
    private val _APValLabel: Label
    private var _APVal = 0
    private val _slotWidth = 52
    private val _slotHeight = 52
    private val _observers: Array<InventoryObserver?>
    private val _inventorySlotTooltip: InventorySlotTooltip

    fun resetEquipSlots() {
        _DPVal = 0
        _APVal = 0
        _DPValLabel.setText(_DPVal.toString())
        notify(_DPVal.toString(), InventoryEvent.UPDATED_DP)
        _APValLabel.setText(_APVal.toString())
        notify(_APVal.toString(), InventoryEvent.UPDATED_AP)
    }

    fun doesInventoryHaveSpace(): Boolean {
        val sourceCells = inventorySlotTable.cells
        var index = 0
        while (index < sourceCells.size) {
            val inventorySlot = sourceCells[index].actor as InventorySlot?
            if (inventorySlot == null) {
                index++
                continue
            }
            val numItems = inventorySlot.numItems
            if (numItems == 0) {
                return true
            } else {
                index++
            }
            index++
        }
        return false
    }

    fun addEntityToInventory(entity: Entity, itemName: String?) {
        val sourceCells = inventorySlotTable.cells
        var index = 0
        while (index < sourceCells.size) {
            val inventorySlot = sourceCells[index].actor as InventorySlot?
            if (inventorySlot == null) {
                index++
                continue
            }
            val numItems = inventorySlot.numItems
            if (numItems == 0) {
                val inventoryItem = InventoryItemFactory.instance!!.getInventoryItem(ItemTypeID.valueOf(entity.entityConfig!!.itemTypeID!!))
                inventoryItem.name = itemName
                inventorySlot.add(inventoryItem)
                dragAndDrop.addSource(InventorySlotSource(inventorySlot, dragAndDrop))
                break
            }
            index++
        }
    }

    fun removeQuestItemFromInventory(questID: String?) {
        val sourceCells = inventorySlotTable.cells
        for (index in 0 until sourceCells.size) {
            val inventorySlot = sourceCells[index].actor as InventorySlot? ?: continue
            val item = inventorySlot.topInventoryItem ?: continue
            val inventoryItemName = item.name
            if (inventoryItemName != null && inventoryItemName == questID) {
                inventorySlot.clearAllInventoryItems(false)
            }
        }
    }

    override fun onNotify(slot: InventorySlot, event: SlotEvent) {
        when (event) {
            SlotEvent.ADDED_ITEM -> {
                val addItem = slot.topInventoryItem ?: return
                if (addItem.isInventoryItemOffensive) {
                    _APVal += addItem.itemUseTypeValue
                    _APValLabel.setText(_APVal.toString())
                    notify(_APVal.toString(), InventoryEvent.UPDATED_AP)
                    if (addItem.isInventoryItemOffensiveWand) {
                        notify(java.lang.String.valueOf(addItem.itemUseTypeValue), InventoryEvent.ADD_WAND_AP)
                    }
                } else if (addItem.isInventoryItemDefensive) {
                    _DPVal += addItem.itemUseTypeValue
                    _DPValLabel.setText(_DPVal.toString())
                    notify(_DPVal.toString(), InventoryEvent.UPDATED_DP)
                }
            }
            SlotEvent.REMOVED_ITEM -> {
                val removeItem = slot.topInventoryItem ?: return
                if (removeItem.isInventoryItemOffensive) {
                    _APVal -= removeItem.itemUseTypeValue
                    _APValLabel.setText(_APVal.toString())
                    notify(_APVal.toString(), InventoryEvent.UPDATED_AP)
                    if (removeItem.isInventoryItemOffensiveWand) {
                        notify(java.lang.String.valueOf(removeItem.itemUseTypeValue), InventoryEvent.REMOVE_WAND_AP)
                    }
                } else if (removeItem.isInventoryItemDefensive) {
                    _DPVal -= removeItem.itemUseTypeValue
                    _DPValLabel.setText(_DPVal.toString())
                    notify(_DPVal.toString(), InventoryEvent.UPDATED_DP)
                }
            }
        }
    }

    override fun addObserver(inventoryObserver: InventoryObserver) {
        _observers.add(inventoryObserver)
    }

    override fun removeObserver(inventoryObserver: InventoryObserver) {
        _observers.removeValue(inventoryObserver, true)
    }

    override fun removeAllObservers() {
        for (observer in _observers) {
            _observers.removeValue(observer, true)
        }
    }

    override fun notify(value: String, event: InventoryEvent) {
        for (observer in _observers) {
            observer!!.onNotify(value, event)
        }
    }

    companion object {
        const val _numSlots = 50
        const val PLAYER_INVENTORY = "Player_Inventory"
        const val STORE_INVENTORY = "Store_Inventory"
        fun clearInventoryItems(targetTable: Table?) {
            val cells = targetTable!!.cells
            for (i in 0 until cells.size) {
                val inventorySlot = cells[i].actor as InventorySlot? ?: continue
                inventorySlot.clearAllInventoryItems(false)
            }
        }

        fun removeInventoryItems(name: String?, inventoryTable: Table): Array<InventoryItemLocation> {
            val cells = inventoryTable.cells
            val items = Array<InventoryItemLocation>()
            for (i in 0 until cells.size) {
                val inventorySlot = cells[i].actor as InventorySlot? ?: continue
                inventorySlot.removeAllInventoryItemsWithName(name)
            }
            return items
        }

        fun populateInventory(targetTable: Table?, inventoryItems: Array<InventoryItemLocation>, draganddrop: DragAndDrop?, defaultName: String?, disableNonDefaultItems: Boolean) {
            clearInventoryItems(targetTable)
            val cells = targetTable!!.cells
            for (i in 0 until inventoryItems.size) {
                val itemLocation = inventoryItems[i]
                val itemTypeID = ItemTypeID.valueOf(itemLocation.itemTypeAtLocation!!)
                val inventorySlot = cells[itemLocation.locationIndex].actor as InventorySlot
                for (index in 0 until itemLocation.numberItemsAtLocation) {
                    val item = InventoryItemFactory.instance!!.getInventoryItem(itemTypeID)
                    val itemName = itemLocation.itemNameProperty
                    if (itemName == null || itemName.isEmpty()) {
                        item.name = defaultName
                    } else {
                        item.name = itemName
                    }
                    inventorySlot.add(item)
                    if (item.name.equals(defaultName, ignoreCase = true)) {
                        draganddrop!!.addSource(InventorySlotSource(inventorySlot, draganddrop))
                    } else if (disableNonDefaultItems == false) {
                        draganddrop!!.addSource(InventorySlotSource(inventorySlot, draganddrop))
                    }
                }
            }
        }

        fun getInventory(targetTable: Table?): Array<InventoryItemLocation> {
            val cells = targetTable!!.cells
            val items = Array<InventoryItemLocation>()
            for (i in 0 until cells.size) {
                val inventorySlot = cells[i].actor as InventorySlot? ?: continue
                val numItems = inventorySlot.numItems
                if (numItems > 0) {
                    items.add(InventoryItemLocation(
                            i,
                            inventorySlot.topInventoryItem?.itemTypeID.toString(),
                            numItems,
                            inventorySlot.topInventoryItem?.name))
                }
            }
            return items
        }

        fun getInventoryFiltered(targetTable: Table, filterOutName: String?): Array<InventoryItemLocation> {
            val cells = targetTable.cells
            val items = Array<InventoryItemLocation>()
            for (i in 0 until cells.size) {
                val inventorySlot = cells[i].actor as InventorySlot? ?: continue
                val numItems = inventorySlot.numItems
                if (numItems > 0) {
                    val topItemName = inventorySlot.topInventoryItem?.name
                    if (topItemName.equals(filterOutName, ignoreCase = true)) continue
                    //System.out.println("[i] " + i + " itemtype: " + inventorySlot.getTopInventoryItem().getItemTypeID().toString() + " numItems " + numItems);
                    items.add(InventoryItemLocation(
                            i,
                            inventorySlot.topInventoryItem?.itemTypeID.toString(),
                            numItems,
                            inventorySlot.topInventoryItem?.name))
                }
            }
            return items
        }

        fun getInventory(targetTable: Table, name: String?): Array<InventoryItemLocation> {
            val cells = targetTable.cells
            val items = Array<InventoryItemLocation>()
            for (i in 0 until cells.size) {
                val inventorySlot = cells[i].actor as InventorySlot? ?: continue
                val numItems = inventorySlot.getNumItems(name)
                if (numItems > 0) {
                    //System.out.println("[i] " + i + " itemtype: " + inventorySlot.getTopInventoryItem().getItemTypeID().toString() + " numItems " + numItems);
                    items.add(InventoryItemLocation(
                            i,
                            inventorySlot.topInventoryItem?.itemTypeID.toString(),
                            numItems,
                            name))
                }
            }
            return items
        }

        fun getInventoryFiltered(sourceTable: Table, targetTable: Table, filterOutName: String?): Array<InventoryItemLocation> {
            val items = getInventoryFiltered(targetTable, filterOutName)
            val sourceCells = sourceTable.cells
            var index = 0
            for (item in items) {
                while (index < sourceCells.size) {
                    val inventorySlot = sourceCells[index].actor as InventorySlot?
                    if (inventorySlot == null) {
                        index++
                        continue
                    }
                    val numItems = inventorySlot.numItems
                    if (numItems == 0) {
                        item.locationIndex = index
                        //System.out.println("[index] " + index + " itemtype: " + item.getItemTypeAtLocation() + " numItems " + numItems);
                        index++
                        break
                    }
                    index++
                }
                if (index == sourceCells.size) {
                    //System.out.println("[index] " + index + " itemtype: " + item.getItemTypeAtLocation() + " numItems " + item.getNumberItemsAtLocation());
                    item.locationIndex = index - 1
                }
            }
            return items
        }

        fun setInventoryItemNames(targetTable: Table, name: String?) {
            val cells = targetTable.cells
            for (i in 0 until cells.size) {
                val inventorySlot = cells[i].actor as InventorySlot? ?: continue
                inventorySlot.updateAllInventoryItemNames(name)
            }
        }
    }

    init {
        _observers = Array()
        dragAndDrop = DragAndDrop()
        inventoryActors = Array()

        //create
        inventorySlotTable = Table()
        inventorySlotTable.name = "Inventory_Slot_Table"
        _playerSlotsTable = Table()
        equipSlotTable = Table()
        equipSlotTable.name = "Equipment_Slot_Table"
        equipSlotTable.defaults().space(10f)
        _inventorySlotTooltip = InventorySlotTooltip(Utility.STATUSUI_SKIN)
        val DPLabel = Label("Defense: ", Utility.STATUSUI_SKIN)
        _DPValLabel = Label(_DPVal.toString(), Utility.STATUSUI_SKIN)
        val APLabel = Label("Attack : ", Utility.STATUSUI_SKIN)
        _APValLabel = Label(_APVal.toString(), Utility.STATUSUI_SKIN)
        val labelTable = Table()
        labelTable.add(DPLabel).align(Align.left)
        labelTable.add(_DPValLabel).align(Align.center)
        labelTable.row()
        labelTable.row()
        labelTable.add(APLabel).align(Align.left)
        labelTable.add(_APValLabel).align(Align.center)
        val headSlot = InventorySlot(
                ItemUseType.ARMOR_HELMET.value,
                Image(Utility.ITEMS_TEXTUREATLAS.findRegion("inv_helmet")))
        val leftArmSlot = InventorySlot(
                ItemUseType.WEAPON_ONEHAND.value or
                        ItemUseType.WEAPON_TWOHAND.value or
                        ItemUseType.ARMOR_SHIELD.value or
                        ItemUseType.WAND_ONEHAND.value or
                        ItemUseType.WAND_TWOHAND.value,
                Image(Utility.ITEMS_TEXTUREATLAS.findRegion("inv_weapon"))
        )
        val rightArmSlot = InventorySlot(
                ItemUseType.WEAPON_ONEHAND.value or
                        ItemUseType.WEAPON_TWOHAND.value or
                        ItemUseType.ARMOR_SHIELD.value or
                        ItemUseType.WAND_ONEHAND.value or
                        ItemUseType.WAND_TWOHAND.value,
                Image(Utility.ITEMS_TEXTUREATLAS.findRegion("inv_shield"))
        )
        val chestSlot = InventorySlot(
                ItemUseType.ARMOR_CHEST.value,
                Image(Utility.ITEMS_TEXTUREATLAS.findRegion("inv_chest")))
        val legsSlot = InventorySlot(
                ItemUseType.ARMOR_FEET.value,
                Image(Utility.ITEMS_TEXTUREATLAS.findRegion("inv_boot")))
        headSlot.addListener(InventorySlotTooltipListener(_inventorySlotTooltip))
        leftArmSlot.addListener(InventorySlotTooltipListener(_inventorySlotTooltip))
        rightArmSlot.addListener(InventorySlotTooltipListener(_inventorySlotTooltip))
        chestSlot.addListener(InventorySlotTooltipListener(_inventorySlotTooltip))
        legsSlot.addListener(InventorySlotTooltipListener(_inventorySlotTooltip))
        headSlot.addObserver(this)
        leftArmSlot.addObserver(this)
        rightArmSlot.addObserver(this)
        chestSlot.addObserver(this)
        legsSlot.addObserver(this)
        dragAndDrop.addTarget(InventorySlotTarget(headSlot))
        dragAndDrop.addTarget(InventorySlotTarget(leftArmSlot))
        dragAndDrop.addTarget(InventorySlotTarget(chestSlot))
        dragAndDrop.addTarget(InventorySlotTarget(rightArmSlot))
        dragAndDrop.addTarget(InventorySlotTarget(legsSlot))
        _playerSlotsTable.background = Image(NinePatch(Utility.STATUSUI_TEXTUREATLAS.createPatch("dialog"))).drawable

        //layout
        for (i in 1.._numSlots) {
            val inventorySlot = InventorySlot()
            inventorySlot.addListener(InventorySlotTooltipListener(_inventorySlotTooltip))
            dragAndDrop.addTarget(InventorySlotTarget(inventorySlot))
            inventorySlotTable.add(inventorySlot).size(_slotWidth.toFloat(), _slotHeight.toFloat())
            inventorySlot.addListener(object : ClickListener() {
                override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                    super.touchUp(event, x, y, pointer, button)
                    if (tapCount == 2) {
                        val slot = event.listenerActor as InventorySlot
                        if (slot.hasItem()) {
                            val item = slot.topInventoryItem
                            if (item!!.isConsumable) {
                                val itemInfo: String = item.itemUseType.toString() + Component.MESSAGE_TOKEN + item.itemUseTypeValue.toString()
                                this@InventoryUI.notify(itemInfo, InventoryEvent.ITEM_CONSUMED)
                                slot.removeActor(item)
                                slot.remove(item)
                            }
                        }
                    }
                }
            }
            )
            if (i % _lengthSlotRow == 0) {
                inventorySlotTable.row()
            }
        }
        equipSlotTable.add()
        equipSlotTable.add(headSlot).size(_slotWidth.toFloat(), _slotHeight.toFloat())
        equipSlotTable.row()
        equipSlotTable.add(leftArmSlot).size(_slotWidth.toFloat(), _slotHeight.toFloat())
        equipSlotTable.add(chestSlot).size(_slotWidth.toFloat(), _slotHeight.toFloat())
        equipSlotTable.add(rightArmSlot).size(_slotWidth.toFloat(), _slotHeight.toFloat())
        equipSlotTable.row()
        equipSlotTable.add()
        equipSlotTable.right().add(legsSlot).size(_slotWidth.toFloat(), _slotHeight.toFloat())
        _playerSlotsTable.add(equipSlotTable)
        inventoryActors.add(_inventorySlotTooltip)
        this.add(_playerSlotsTable).padBottom(20f)
        this.add(labelTable)
        row()
        this.add(inventorySlotTable).colspan(2)
        row()
        pack()
    }
}