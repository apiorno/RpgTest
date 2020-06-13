package com.mygdx.game.widgets

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.mygdx.game.Utility
import com.mygdx.game.temporal.InventorySlotObserver
import com.mygdx.game.temporal.InventorySlotObserver.*
import com.mygdx.game.temporal.StoreInventoryObserver
import com.mygdx.game.temporal.StoreInventoryObserver.*
import com.mygdx.game.temporal.StoreInventorySubject

class StoreInventoryUI : Window("Store Inventory", Utility.STATUSUI_SKIN, "solidbackground"), InventorySlotObserver, StoreInventorySubject {
    private val numStoreInventorySlots = 30
    private val lengthSlotRow = 10
    val inventorySlotTable: Table
    private val playerInventorySlotTable: Table
    private val dragAndDrop: DragAndDrop
    val inventoryActors: Array<Actor>
    private val slotWidth = 52
    private val slotHeight = 52
    private val inventorySlotTooltip: InventorySlotTooltip
    private val sellTotalLabel: Label
    private val buyTotalLabel: Label
    private val playerTotalGP: Label
    private var tradeInVal = 0
    private var fullValue = 0
    private var playerTotal = 0
    private val sellButton: Button
    private val buyButton: Button
    var closeButton: TextButton
    private val buttons: Table
    private val totalLabels: Table
    private val observers: Array<StoreInventoryObserver> = Array()
    private val json: Json = Json()

    fun loadPlayerInventory(playerInventoryItems: Array<InventoryItemLocation>) {
        InventoryUI.populateInventory(playerInventorySlotTable, playerInventoryItems, dragAndDrop, InventoryUI.PLAYER_INVENTORY, true)
    }

    fun loadStoreInventory(storeInventoryItems: Array<InventoryItemLocation>) {
        InventoryUI.populateInventory(inventorySlotTable, storeInventoryItems, dragAndDrop, InventoryUI.STORE_INVENTORY, false)
    }

    fun savePlayerInventory() {
        val playerItemsInPlayerInventory: Array<InventoryItemLocation> = InventoryUI.getInventoryFiltered(playerInventorySlotTable, InventoryUI.STORE_INVENTORY)
        val playerItemsInStoreInventory: Array<InventoryItemLocation> = InventoryUI.getInventoryFiltered(playerInventorySlotTable, inventorySlotTable, InventoryUI.STORE_INVENTORY)
        playerItemsInPlayerInventory.addAll(playerItemsInStoreInventory)
        this@StoreInventoryUI.notify(json.toJson(playerItemsInPlayerInventory), StoreInventoryEvent.PLAYER_INVENTORY_UPDATED)
    }

    fun cleanupStoreInventory() {
        InventoryUI.removeInventoryItems(InventoryUI.STORE_INVENTORY, playerInventorySlotTable)
        InventoryUI.removeInventoryItems(InventoryUI.PLAYER_INVENTORY, inventorySlotTable)
    }

    override fun onNotify(slot: InventorySlot, event: SlotEvent?) {
        when (event) {
            SlotEvent.ADDED_ITEM -> {
                //moving from player inventory to store inventory to sell
                if (slot.topInventoryItem?.name.equals(InventoryUI.PLAYER_INVENTORY, ignoreCase = true) &&
                        slot.name.equals(InventoryUI.STORE_INVENTORY, ignoreCase = true)) {
                    tradeInVal += slot.topInventoryItem?.tradeValue!!
                    sellTotalLabel.setText("$SELL : $tradeInVal$GP")
                }
                //moving from store inventory to player inventory to buy
                if (slot.topInventoryItem?.name.equals(InventoryUI.STORE_INVENTORY, ignoreCase = true) &&
                        slot.name.equals(InventoryUI.PLAYER_INVENTORY, ignoreCase = true)) {
                    fullValue += slot.topInventoryItem?.itemValue!!
                    buyTotalLabel.setText("$BUY : $fullValue$GP")
                }
            }
            SlotEvent.REMOVED_ITEM -> {
                if (slot.topInventoryItem?.name.equals(InventoryUI.PLAYER_INVENTORY, ignoreCase = true) &&
                        slot.name.equals(InventoryUI.STORE_INVENTORY, ignoreCase = true)) {
                    tradeInVal -= slot.topInventoryItem?.tradeValue!!
                    sellTotalLabel.setText("$SELL : $tradeInVal$GP")
                }
                if (slot.topInventoryItem?.name.equals(InventoryUI.STORE_INVENTORY, ignoreCase = true) &&
                        slot.name.equals(InventoryUI.PLAYER_INVENTORY, ignoreCase = true)) {
                    fullValue -= slot.topInventoryItem?.itemValue!!
                    buyTotalLabel.setText("$BUY : $fullValue$GP")
                }
            }
        }
        checkButtonStates()
    }

    fun checkButtonStates() {
        if (tradeInVal <= 0) {
            disableButton(sellButton, true)
        } else {
            disableButton(sellButton, false)
        }
        if (fullValue <= 0 || playerTotal < fullValue) {
            disableButton(buyButton, true)
        } else {
            disableButton(buyButton, false)
        }
    }

    fun setPlayerGP(value: Int) {
        playerTotal = value
        playerTotalGP.setText("$PLAYER_TOTAL : $playerTotal$GP")
    }

    private fun disableButton(button: Button, disable: Boolean) {
        if (disable) {
            button.isDisabled = true
            button.touchable = Touchable.disabled
        } else {
            button.isDisabled = false
            button.touchable = Touchable.enabled
        }
    }

    override fun addObserver(storeObserver: StoreInventoryObserver) {
        observers.add(storeObserver)
    }

    override fun removeObserver(storeObserver: StoreInventoryObserver) {
        observers.removeValue(storeObserver, true)
    }

    override fun removeAllObservers() {
        for (observer in observers) {
            observers.removeValue(observer, true)
        }
    }

    override fun notify(value: String?, event: StoreInventoryEvent?) {
        for (observer in observers) {
            observer.onNotify(value, event)
        }
    }

    companion object {
        private const val SELL = "SELL"
        private const val BUY = "BUY"
        private const val GP = " GP"
        private const val PLAYER_TOTAL = "Player Total"
    }

    init {
        setFillParent(true)

        //create
        dragAndDrop = DragAndDrop()
        inventoryActors = Array()
        inventorySlotTable = Table()
        inventorySlotTable.name = InventoryUI.STORE_INVENTORY
        playerInventorySlotTable = Table()
        playerInventorySlotTable.name = InventoryUI.PLAYER_INVENTORY
        inventorySlotTooltip = InventorySlotTooltip(Utility.STATUSUI_SKIN)
        sellButton = TextButton(SELL, Utility.STATUSUI_SKIN, "inventory")
        disableButton(sellButton, true)
        sellTotalLabel = Label("$SELL : $tradeInVal$GP", Utility.STATUSUI_SKIN)
        sellTotalLabel.setAlignment(Align.center)
        buyTotalLabel = Label("$BUY : $fullValue$GP", Utility.STATUSUI_SKIN)
        buyTotalLabel.setAlignment(Align.center)
        playerTotalGP = Label("$PLAYER_TOTAL : $playerTotal$GP", Utility.STATUSUI_SKIN)
        buyButton = TextButton(BUY, Utility.STATUSUI_SKIN, "inventory")
        disableButton(buyButton, true)
        closeButton = TextButton("X", Utility.STATUSUI_SKIN)
        buttons = Table()
        buttons.defaults().expand().fill()
        buttons.add<Button>(sellButton).padLeft(10f).padRight(10f)
        buttons.add<Button>(buyButton).padLeft(10f).padRight(10f)
        totalLabels = Table()
        totalLabels.defaults().expand().fill()
        totalLabels.add(sellTotalLabel).padLeft(40f)
        totalLabels.add()
        totalLabels.add(buyTotalLabel).padRight(40f)

        //layout
        for (i in 1..numStoreInventorySlots) {
            val inventorySlot = InventorySlot()
            inventorySlot.addListener(InventorySlotTooltipListener(inventorySlotTooltip))
            inventorySlot.addObserver(this)
            inventorySlot.name = InventoryUI.STORE_INVENTORY
            dragAndDrop.addTarget(InventorySlotTarget(inventorySlot))
            inventorySlotTable.add(inventorySlot).size(slotWidth.toFloat(), slotHeight.toFloat())
            if (i % lengthSlotRow == 0) {
                inventorySlotTable.row()
            }
        }
        for (i in 1..InventoryUI.numSlots) {
            val inventorySlot = InventorySlot()
            inventorySlot.addListener(InventorySlotTooltipListener(inventorySlotTooltip))
            inventorySlot.addObserver(this)
            inventorySlot.name = InventoryUI.PLAYER_INVENTORY
            dragAndDrop.addTarget(InventorySlotTarget(inventorySlot))
            playerInventorySlotTable.add(inventorySlot).size(slotWidth.toFloat(), slotHeight.toFloat())
            if (i % lengthSlotRow == 0) {
                playerInventorySlotTable.row()
            }
        }
        inventoryActors.add(inventorySlotTooltip)
        this.add()
        this.add(closeButton)
        row()

        //this.debugAll();
        defaults().expand().fill()
        this.add(inventorySlotTable).pad(10f, 10f, 10f, 10f).row()
        this.add(buttons).row()
        this.add(totalLabels).row()
        this.add(playerInventorySlotTable).pad(10f, 10f, 10f, 10f).row()
        this.add(playerTotalGP)
        pack()

        //Listeners
        buyButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                if (fullValue in 1..playerTotal) {
                    playerTotal -= fullValue
                    this@StoreInventoryUI.notify(playerTotal.toString(), StoreInventoryEvent.PLAYER_GP_TOTAL_UPDATED)
                    fullValue = 0
                    buyTotalLabel.setText("$BUY : $fullValue$GP")
                    checkButtonStates()

                    //Make sure we update the owner of the items
                    InventoryUI.setInventoryItemNames(playerInventorySlotTable, InventoryUI.PLAYER_INVENTORY)
                    savePlayerInventory()
                }
            }
        }
        )
        sellButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                if (tradeInVal > 0) {
                    playerTotal += tradeInVal
                    this@StoreInventoryUI.notify(playerTotal.toString(), StoreInventoryEvent.PLAYER_GP_TOTAL_UPDATED)
                    tradeInVal = 0
                    sellTotalLabel.setText("$SELL : $tradeInVal$GP")
                    checkButtonStates()

                    //Remove sold items
                    val cells = inventorySlotTable.cells
                    for (i in 0 until cells.size) {
                        val inventorySlot = cells[i].actor as InventorySlot
                        if (inventorySlot.hasItem() &&
                                inventorySlot.topInventoryItem?.name.equals(InventoryUI.PLAYER_INVENTORY, ignoreCase = true)) {
                            inventorySlot.clearAllInventoryItems(false)
                        }
                    }
                    savePlayerInventory()
                }
            }
        }
        )
    }
}