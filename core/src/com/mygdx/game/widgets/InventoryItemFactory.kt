package com.mygdx.game.widgets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue
import com.badlogic.gdx.utils.Scaling
import com.mygdx.game.Utility
import com.mygdx.game.widgets.InventoryItem.*
import java.util.*

class InventoryItemFactory private constructor() {
    private val json = Json()
    private val INVENTORY_ITEM = "scripts/inventory_items.json"
    private val inventoryItemList: Hashtable<ItemTypeID?, InventoryItem>
    fun getInventoryItem(inventoryItemType: ItemTypeID?): InventoryItem {
        val item = InventoryItem(inventoryItemList[inventoryItemType])
        item.drawable = TextureRegionDrawable(Utility.ITEMS_TEXTUREATLAS.findRegion(item.itemTypeID.toString()))
        item.setScaling(Scaling.none)
        return item
    } /*
    public void testAllItemLoad(){
        for(ItemTypeID itemTypeID : ItemTypeID.values()) {
            InventoryItem item = new InventoryItem(inventoryItemList.get(itemTypeID));
            item.setDrawable(new TextureRegionDrawable(PlayerHUD.itemsTextureAtlas.findRegion(item.getItemTypeID().toString())));
            item.setScaling(Scaling.none);
        }
    }*/

    companion object {
        private var uniqueInstance: InventoryItemFactory? = null
        @kotlin.jvm.JvmStatic
        val instance: InventoryItemFactory?
            get() {
                if (uniqueInstance == null) {
                    uniqueInstance = InventoryItemFactory()
                }
                return uniqueInstance
            }
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