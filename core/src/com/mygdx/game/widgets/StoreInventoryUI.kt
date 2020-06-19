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
import com.mygdx.game.widgets.InventorySlotObserver.SlotEvent
import com.mygdx.game.widgets.StoreInventoryObserver.StoreInventoryEvent
import com.mygdx.game.Utility

class StoreInventoryUI : Window("Store Inventory", Utility.STATUSUI_SKIN, "solidbackground"), InventorySlotObserver, StoreInventorySubject {
    private val _numStoreInventorySlots = 30
    private val _lengthSlotRow = 10
    val inventorySlotTable: Table
    private val _playerInventorySlotTable: Table
    private val _dragAndDrop: DragAndDrop
    val inventoryActors: Array<Actor>
    private val _slotWidth = 52
    private val _slotHeight = 52
    private val _inventorySlotTooltip: InventorySlotTooltip
    private val _sellTotalLabel: Label
    private val _buyTotalLabel: Label
    private val _playerTotalGP: Label
    private var _tradeInVal = 0
    private var _fullValue = 0
    private var _playerTotal = 0
    private val _sellButton: Button
    private val _buyButton: Button
    var closeButton: TextButton
    private val _buttons: Table
    private val _totalLabels: Table
    private val _observers: Array<StoreInventoryObserver>
    private val _json: Json

    fun loadPlayerInventory(playerInventoryItems: Array<InventoryItemLocation>) {
        InventoryUI.Companion.populateInventory(_playerInventorySlotTable, playerInventoryItems, _dragAndDrop, InventoryUI.Companion.PLAYER_INVENTORY, true)
    }

    fun loadStoreInventory(storeInventoryItems: Array<InventoryItemLocation>) {
        InventoryUI.Companion.populateInventory(inventorySlotTable, storeInventoryItems, _dragAndDrop, InventoryUI.Companion.STORE_INVENTORY, false)
    }

    fun savePlayerInventory() {
        val playerItemsInPlayerInventory: Array<InventoryItemLocation> = InventoryUI.Companion.getInventoryFiltered(_playerInventorySlotTable, InventoryUI.Companion.STORE_INVENTORY)
        val playerItemsInStoreInventory: Array<InventoryItemLocation> = InventoryUI.Companion.getInventoryFiltered(_playerInventorySlotTable, inventorySlotTable, InventoryUI.Companion.STORE_INVENTORY)
        playerItemsInPlayerInventory.addAll(playerItemsInStoreInventory)
        this@StoreInventoryUI.notify(_json.toJson(playerItemsInPlayerInventory), StoreInventoryEvent.PLAYER_INVENTORY_UPDATED)
    }

    fun cleanupStoreInventory() {
        InventoryUI.Companion.removeInventoryItems(InventoryUI.Companion.STORE_INVENTORY, _playerInventorySlotTable)
        InventoryUI.Companion.removeInventoryItems(InventoryUI.Companion.PLAYER_INVENTORY, inventorySlotTable)
    }

    override fun onNotify(slot: InventorySlot, event: SlotEvent) {
        when (event) {
            SlotEvent.ADDED_ITEM -> {
                //moving from player inventory to store inventory to sell
                if (slot.topInventoryItem?.name.equals(InventoryUI.Companion.PLAYER_INVENTORY, ignoreCase = true) &&
                        slot.name.equals(InventoryUI.Companion.STORE_INVENTORY, ignoreCase = true)) {
                    _tradeInVal += slot.topInventoryItem?.tradeValue!!
                    _sellTotalLabel.setText("$SELL : $_tradeInVal$GP")
                }
                //moving from store inventory to player inventory to buy
                if (slot.topInventoryItem?.name.equals(InventoryUI.Companion.STORE_INVENTORY, ignoreCase = true) &&
                        slot.name.equals(InventoryUI.Companion.PLAYER_INVENTORY, ignoreCase = true)) {
                    _fullValue += slot.topInventoryItem?.itemValue!!
                    _buyTotalLabel.setText("$BUY : $_fullValue$GP")
                }
            }
            SlotEvent.REMOVED_ITEM -> {
                if (slot.topInventoryItem?.name.equals(InventoryUI.Companion.PLAYER_INVENTORY, ignoreCase = true) &&
                        slot.name.equals(InventoryUI.Companion.STORE_INVENTORY, ignoreCase = true)) {
                    _tradeInVal -= slot.topInventoryItem?.tradeValue!!
                    _sellTotalLabel.setText("$SELL : $_tradeInVal$GP")
                }
                if (slot.topInventoryItem?.name.equals(InventoryUI.Companion.STORE_INVENTORY, ignoreCase = true) &&
                        slot.name.equals(InventoryUI.Companion.PLAYER_INVENTORY, ignoreCase = true)) {
                    _fullValue -= slot.topInventoryItem?.itemValue!!
                    _buyTotalLabel.setText("$BUY : $_fullValue$GP")
                }
            }
        }
        checkButtonStates()
    }

    fun checkButtonStates() {
        if (_tradeInVal <= 0) {
            disableButton(_sellButton, true)
        } else {
            disableButton(_sellButton, false)
        }
        if (_fullValue <= 0 || _playerTotal < _fullValue) {
            disableButton(_buyButton, true)
        } else {
            disableButton(_buyButton, false)
        }
    }

    fun setPlayerGP(value: Int) {
        _playerTotal = value
        _playerTotalGP.setText("$PLAYER_TOTAL : $_playerTotal$GP")
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
        _observers.add(storeObserver)
    }

    override fun removeObserver(storeObserver: StoreInventoryObserver) {
        _observers.removeValue(storeObserver, true)
    }

    override fun removeAllObservers() {
        for (observer in _observers) {
            _observers.removeValue(observer, true)
        }
    }

    override fun notify(value: String, event: StoreInventoryEvent) {
        for (observer in _observers) {
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
        _observers = Array()
        _json = Json()
        setFillParent(true)

        //create
        _dragAndDrop = DragAndDrop()
        inventoryActors = Array()
        inventorySlotTable = Table()
        inventorySlotTable.name = InventoryUI.Companion.STORE_INVENTORY
        _playerInventorySlotTable = Table()
        _playerInventorySlotTable.name = InventoryUI.Companion.PLAYER_INVENTORY
        _inventorySlotTooltip = InventorySlotTooltip(Utility.STATUSUI_SKIN)
        _sellButton = TextButton(SELL, Utility.STATUSUI_SKIN, "inventory")
        disableButton(_sellButton, true)
        _sellTotalLabel = Label("$SELL : $_tradeInVal$GP", Utility.STATUSUI_SKIN)
        _sellTotalLabel.setAlignment(Align.center)
        _buyTotalLabel = Label("$BUY : $_fullValue$GP", Utility.STATUSUI_SKIN)
        _buyTotalLabel.setAlignment(Align.center)
        _playerTotalGP = Label("$PLAYER_TOTAL : $_playerTotal$GP", Utility.STATUSUI_SKIN)
        _buyButton = TextButton(BUY, Utility.STATUSUI_SKIN, "inventory")
        disableButton(_buyButton, true)
        closeButton = TextButton("X", Utility.STATUSUI_SKIN)
        _buttons = Table()
        _buttons.defaults().expand().fill()
        _buttons.add<Button>(_sellButton).padLeft(10f).padRight(10f)
        _buttons.add<Button>(_buyButton).padLeft(10f).padRight(10f)
        _totalLabels = Table()
        _totalLabels.defaults().expand().fill()
        _totalLabels.add(_sellTotalLabel).padLeft(40f)
        _totalLabels.add()
        _totalLabels.add(_buyTotalLabel).padRight(40f)

        //layout
        for (i in 1.._numStoreInventorySlots) {
            val inventorySlot = InventorySlot()
            inventorySlot.addListener(InventorySlotTooltipListener(_inventorySlotTooltip))
            inventorySlot.addObserver(this)
            inventorySlot.name = InventoryUI.Companion.STORE_INVENTORY
            _dragAndDrop.addTarget(InventorySlotTarget(inventorySlot))
            inventorySlotTable.add(inventorySlot).size(_slotWidth.toFloat(), _slotHeight.toFloat())
            if (i % _lengthSlotRow == 0) {
                inventorySlotTable.row()
            }
        }
        for (i in 1..InventoryUI.Companion._numSlots) {
            val inventorySlot = InventorySlot()
            inventorySlot.addListener(InventorySlotTooltipListener(_inventorySlotTooltip))
            inventorySlot.addObserver(this)
            inventorySlot.name = InventoryUI.Companion.PLAYER_INVENTORY
            _dragAndDrop.addTarget(InventorySlotTarget(inventorySlot))
            _playerInventorySlotTable.add(inventorySlot).size(_slotWidth.toFloat(), _slotHeight.toFloat())
            if (i % _lengthSlotRow == 0) {
                _playerInventorySlotTable.row()
            }
        }
        inventoryActors.add(_inventorySlotTooltip)
        this.add()
        this.add(closeButton)
        row()

        //this.debugAll();
        defaults().expand().fill()
        this.add(inventorySlotTable).pad(10f, 10f, 10f, 10f).row()
        this.add(_buttons).row()
        this.add(_totalLabels).row()
        this.add(_playerInventorySlotTable).pad(10f, 10f, 10f, 10f).row()
        this.add(_playerTotalGP)
        pack()

        //Listeners
        _buyButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                if (_fullValue > 0 && _playerTotal >= _fullValue) {
                    _playerTotal -= _fullValue
                    this@StoreInventoryUI.notify(Integer.toString(_playerTotal), StoreInventoryEvent.PLAYER_GP_TOTAL_UPDATED)
                    _fullValue = 0
                    _buyTotalLabel.setText("$BUY : $_fullValue$GP")
                    checkButtonStates()

                    //Make sure we update the owner of the items
                    InventoryUI.Companion.setInventoryItemNames(_playerInventorySlotTable, InventoryUI.Companion.PLAYER_INVENTORY)
                    savePlayerInventory()
                }
            }
        }
        )
        _sellButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                if (_tradeInVal > 0) {
                    _playerTotal += _tradeInVal
                    this@StoreInventoryUI.notify(Integer.toString(_playerTotal), StoreInventoryEvent.PLAYER_GP_TOTAL_UPDATED)
                    _tradeInVal = 0
                    _sellTotalLabel.setText("$SELL : $_tradeInVal$GP")
                    checkButtonStates()

                    //Remove sold items
                    val cells = inventorySlotTable.cells
                    for (i in 0 until cells.size) {
                        val inventorySlot = cells[i].actor as InventorySlot? ?: continue
                        if (inventorySlot.hasItem() &&
                                inventorySlot.topInventoryItem?.name.equals(InventoryUI.Companion.PLAYER_INVENTORY, ignoreCase = true)) {
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