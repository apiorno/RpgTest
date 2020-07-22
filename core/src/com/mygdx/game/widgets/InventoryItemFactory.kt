package com.mygdx.game.widgets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue
import com.badlogic.gdx.utils.Scaling
import com.mygdx.game.Utility
import com.mygdx.game.widgets.InventoryItem.ItemTypeID
import java.util.*

object InventoryItemFactory{
    private val json = Json()
    private const val INVENTORY_ITEM = "scripts/inventory_items.json"
    private val inventoryItemList: Hashtable<ItemTypeID?, InventoryItem>
    fun getInventoryItem(inventoryItemType: ItemTypeID?): InventoryItem {
        val item = InventoryItem(inventoryItemList[inventoryItemType])
        item.drawable = TextureRegionDrawable(Utility.ITEMS_TEXTUREATLAS.findRegion(item.itemTypeID.toString()))
        item.setScaling(Scaling.none)
        return item
    }


    init {
        @Suppress("UNCHECKED_CAST") val list: ArrayList<JsonValue> = json.fromJson(ArrayList::class.java, Gdx.files.internal(INVENTORY_ITEM)) as ArrayList<JsonValue>
        inventoryItemList = Hashtable()
        for (jsonVal in list) {
            val inventoryItem = json.readValue(InventoryItem::class.java, jsonVal)
            inventoryItemList[inventoryItem.itemTypeID] = inventoryItem
        }
    }
}