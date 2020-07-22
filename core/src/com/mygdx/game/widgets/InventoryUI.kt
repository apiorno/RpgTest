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
    private val lengthSlotRow = 10
    val inventorySlotTable: Table
    private val playerSlotsTable: Table
    val equipSlotTable: Table
    val dragAndDrop: DragAndDrop = DragAndDrop()
    val inventoryActors: Array<Actor> = Array()
    private val dPValLabel: Label
    private var dPVal = 0
    private val aPValLabel: Label
    private var aPVal = 0
    private val slotWidth = 52
    private val slotHeight = 52
    private val observers: Array<InventoryObserver?> = Array()
    private val inventorySlotTooltip: InventorySlotTooltip

    fun resetEquipSlots() {
        dPVal = 0
        aPVal = 0
        dPValLabel.setText(dPVal.toString())
        notify(dPVal.toString(), InventoryEvent.UPDATED_DP)
        aPValLabel.setText(aPVal.toString())
        notify(aPVal.toString(), InventoryEvent.UPDATED_AP)
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
                val inventoryItem = InventoryItemFactory.getInventoryItem(ItemTypeID.valueOf(entity.entityConfig!!.itemTypeID!!))
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
                    aPVal += addItem.itemUseTypeValue
                    aPValLabel.setText(aPVal.toString())
                    notify(aPVal.toString(), InventoryEvent.UPDATED_AP)
                    if (addItem.isInventoryItemOffensiveWand) {
                        notify(java.lang.String.valueOf(addItem.itemUseTypeValue), InventoryEvent.ADD_WAND_AP)
                    }
                } else if (addItem.isInventoryItemDefensive) {
                    dPVal += addItem.itemUseTypeValue
                    dPValLabel.setText(dPVal.toString())
                    notify(dPVal.toString(), InventoryEvent.UPDATED_DP)
                }
            }
            SlotEvent.REMOVED_ITEM -> {
                val removeItem = slot.topInventoryItem ?: return
                if (removeItem.isInventoryItemOffensive) {
                    aPVal -= removeItem.itemUseTypeValue
                    aPValLabel.setText(aPVal.toString())
                    notify(aPVal.toString(), InventoryEvent.UPDATED_AP)
                    if (removeItem.isInventoryItemOffensiveWand) {
                        notify(java.lang.String.valueOf(removeItem.itemUseTypeValue), InventoryEvent.REMOVE_WAND_AP)
                    }
                } else if (removeItem.isInventoryItemDefensive) {
                    dPVal -= removeItem.itemUseTypeValue
                    dPValLabel.setText(dPVal.toString())
                    notify(dPVal.toString(), InventoryEvent.UPDATED_DP)
                }
            }
        }
    }

    override fun addObserver(inventoryObserver: InventoryObserver) {
        observers.add(inventoryObserver)
    }

    override fun removeObserver(inventoryObserver: InventoryObserver) {
        observers.removeValue(inventoryObserver, true)
    }

    override fun removeAllObservers() {
        observers.forEach {
            observers.removeValue(it, true)
        }
    }

    override fun notify(value: String, event: InventoryEvent) {
        observers.forEach {
            it?.onNotify(value, event)
        }
    }

    companion object {
        const val numSlots = 50
        const val PLAYER_INVENTORY = "Player_Inventory"
        const val STORE_INVENTORY = "Store_Inventory"
        fun clearInventoryItems(targetTable: Table?) {
            val cells = targetTable!!.cells
            cells.forEach {
                val inventorySlot = it.actor as InventorySlot?
                inventorySlot?.clearAllInventoryItems(false)
            }
        }

        fun removeInventoryItems(name: String?, inventoryTable: Table){
            val cells = inventoryTable.cells
            cells.forEach {
                val inventorySlot = it.actor as InventorySlot?
                inventorySlot?.removeAllInventoryItemsWithName(name)
            }
        }

        fun populateInventory(targetTable: Table?, inventoryItems: Array<InventoryItemLocation>, draganddrop: DragAndDrop?, defaultName: String?, disableNonDefaultItems: Boolean) {
            clearInventoryItems(targetTable)
            val cells = targetTable!!.cells
            inventoryItems.forEach {
                val itemTypeID = ItemTypeID.valueOf(it.itemTypeAtLocation!!)
                val inventorySlot = cells[it.locationIndex].actor as InventorySlot
                for (index in 0 until it.numberItemsAtLocation) {
                    val item = InventoryItemFactory.getInventoryItem(itemTypeID)
                    val itemName = it.itemNameProperty
                    if (itemName == null || itemName.isEmpty()) {
                        item.name = defaultName
                    } else {
                        item.name = itemName
                    }
                    inventorySlot.add(item)
                    if (item.name.equals(defaultName, ignoreCase = true)) {
                        draganddrop!!.addSource(InventorySlotSource(inventorySlot, draganddrop))
                    } else if (!disableNonDefaultItems) {
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
            items.forEach {
                while (index < sourceCells.size) {
                    val inventorySlot = sourceCells[index].actor as InventorySlot?
                    if (inventorySlot == null) {
                        index++
                        continue
                    }
                    val numItems = inventorySlot.numItems
                    if (numItems == 0) {
                        it.locationIndex = index
                        //System.out.println("[index] " + index + " itemtype: " + item.getItemTypeAtLocation() + " numItems " + numItems);
                        index++
                        break
                    }
                    index++
                }
                if (index == sourceCells.size) {
                    //System.out.println("[index] " + index + " itemtype: " + item.getItemTypeAtLocation() + " numItems " + item.getNumberItemsAtLocation());
                    it.locationIndex = index - 1
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

        //create
        inventorySlotTable = Table()
        inventorySlotTable.name = "Inventory_Slot_Table"
        playerSlotsTable = Table()
        equipSlotTable = Table()
        equipSlotTable.name = "Equipment_Slot_Table"
        equipSlotTable.defaults().space(10f)
        inventorySlotTooltip = InventorySlotTooltip(Utility.STATUSUI_SKIN)
        val dPLabel = Label("Defense: ", Utility.STATUSUI_SKIN)
        dPValLabel = Label(dPVal.toString(), Utility.STATUSUI_SKIN)
        val aPLabel = Label("Attack : ", Utility.STATUSUI_SKIN)
        aPValLabel = Label(aPVal.toString(), Utility.STATUSUI_SKIN)
        val labelTable = Table()
        labelTable.add(dPLabel).align(Align.left)
        labelTable.add(dPValLabel).align(Align.center)
        labelTable.row()
        labelTable.row()
        labelTable.add(aPLabel).align(Align.left)
        labelTable.add(aPValLabel).align(Align.center)
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
        headSlot.addListener(InventorySlotTooltipListener(inventorySlotTooltip))
        leftArmSlot.addListener(InventorySlotTooltipListener(inventorySlotTooltip))
        rightArmSlot.addListener(InventorySlotTooltipListener(inventorySlotTooltip))
        chestSlot.addListener(InventorySlotTooltipListener(inventorySlotTooltip))
        legsSlot.addListener(InventorySlotTooltipListener(inventorySlotTooltip))
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
        playerSlotsTable.background = Image(NinePatch(Utility.STATUSUI_TEXTUREATLAS.createPatch("dialog"))).drawable

        //layout
        for (i in 1..numSlots) {
            val inventorySlot = InventorySlot()
            inventorySlot.addListener(InventorySlotTooltipListener(inventorySlotTooltip))
            dragAndDrop.addTarget(InventorySlotTarget(inventorySlot))
            inventorySlotTable.add(inventorySlot).size(slotWidth.toFloat(), slotHeight.toFloat())
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
            if (i % lengthSlotRow == 0) {
                inventorySlotTable.row()
            }
        }
        equipSlotTable.add()
        equipSlotTable.add(headSlot).size(slotWidth.toFloat(), slotHeight.toFloat())
        equipSlotTable.row()
        equipSlotTable.add(leftArmSlot).size(slotWidth.toFloat(), slotHeight.toFloat())
        equipSlotTable.add(chestSlot).size(slotWidth.toFloat(), slotHeight.toFloat())
        equipSlotTable.add(rightArmSlot).size(slotWidth.toFloat(), slotHeight.toFloat())
        equipSlotTable.row()
        equipSlotTable.add()
        equipSlotTable.right().add(legsSlot).size(slotWidth.toFloat(), slotHeight.toFloat())
        playerSlotsTable.add(equipSlotTable)
        inventoryActors.add(inventorySlotTooltip)
        this.add(playerSlotsTable).padBottom(20f)
        this.add(labelTable)
        row()
        this.add(inventorySlotTable).colspan(2)
        row()
        pack()
    }
}