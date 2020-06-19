package com.mygdx.game.widgets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue
import com.badlogic.gdx.utils.Scaling
import com.mygdx.game.Utility
import com.mygdx.game.widgets.InventoryItem.ItemTypeID
import java.util.*

class InventoryItemFactory private constructor() {
    private val _json = Json()
    private val INVENTORY_ITEM = "scripts/inventory_items.json"
    private val _inventoryItemList: Hashtable<ItemTypeID?, InventoryItem>
    fun getInventoryItem(inventoryItemType: ItemTypeID?): InventoryItem {
        val item = InventoryItem(_inventoryItemList[inventoryItemType])
        item.drawable = TextureRegionDrawable(Utility.ITEMS_TEXTUREATLAS.findRegion(item.itemTypeID.toString()))
        item.setScaling(Scaling.none)
        return item
    } /*
    public void testAllItemLoad(){
        for(ItemTypeID itemTypeID : ItemTypeID.values()) {
            InventoryItem item = new InventoryItem(_inventoryItemList.get(itemTypeID));
            item.setDrawable(new TextureRegionDrawable(PlayerHUD.itemsTextureAtlas.findRegion(item.getItemTypeID().toString())));
            item.setScaling(Scaling.none);
        }
    }*/

    companion object {
        private var _instance: InventoryItemFactory? = null
        @kotlin.jvm.JvmStatic
        val instance: InventoryItemFactory?
            get() {
                if (_instance == null) {
                    _instance = InventoryItemFactory()
                }
                return _instance
            }
    }

    init {
        @Suppress("UNCHECKED_CAST") val list: ArrayList<JsonValue> = _json.fromJson(ArrayList::class.java, Gdx.files.internal(INVENTORY_ITEM)) as ArrayList<JsonValue>
        _inventoryItemList = Hashtable()
        for (jsonVal in list) {
            val inventoryItem = _json.readValue(InventoryItem::class.java, jsonVal)
            _inventoryItemList[inventoryItem.itemTypeID] = inventoryItem
        }
    }
}