package com.mygdx.game.widgets

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.ui.Image

class InventoryItem : Image {
    enum class ItemAttribute(val value: Int) {
        CONSUMABLE(1), EQUIPPABLE(2), STACKABLE(4);

    }

    enum class ItemUseType(val value: Int) {
        ITEM_RESTORE_HEALTH(1), ITEM_RESTORE_MP(2), ITEM_DAMAGE(4), WEAPON_ONEHAND(8), WEAPON_TWOHAND(16), WAND_ONEHAND(32), WAND_TWOHAND(64), ARMOR_SHIELD(128), ARMOR_HELMET(256), ARMOR_CHEST(512), ARMOR_FEET(1024), QUEST_ITEM(2048);

    }

    enum class ItemTypeID {
        ARMOR01, ARMOR02, ARMOR03, ARMOR04, ARMOR05, BOOTS01, BOOTS02, BOOTS03, BOOTS04, BOOTS05, HELMET01, HELMET02, HELMET03, HELMET04, HELMET05, SHIELD01, SHIELD02, SHIELD03, SHIELD04, SHIELD05, WANDS01, WANDS02, WANDS03, WANDS04, WANDS05, WEAPON01, WEAPON02, WEAPON03, WEAPON04, WEAPON05, POTIONS01, POTIONS02, POTIONS03, SCROLL01, SCROLL02, SCROLL03, HERB001, BABY001, HORNS001, FUR001, NONE
    }

    var itemAttributes = 0
    var itemUseType = 0
    var itemUseTypeValue = 0
    var itemTypeID: ItemTypeID? = null
    var itemShortDescription: String? = null
    var itemValue = 0

    constructor(textureRegion: TextureRegion?, itemAttributes: Int, itemTypeID: ItemTypeID?, itemUseType: Int, itemUseTypeValue: Int, itemValue: Int) : super(textureRegion) {
        this.itemTypeID = itemTypeID
        this.itemAttributes = itemAttributes
        this.itemUseType = itemUseType
        this.itemUseTypeValue = itemUseTypeValue
        this.itemValue = itemValue
    }

    constructor() : super() {}
    constructor(inventoryItem: InventoryItem?) : super() {
        itemTypeID = inventoryItem!!.itemTypeID
        itemAttributes = inventoryItem.itemAttributes
        itemUseType = inventoryItem.itemUseType
        itemUseTypeValue = inventoryItem.itemUseTypeValue
        itemShortDescription = inventoryItem.itemShortDescription
        itemValue = inventoryItem.itemValue
    }

    val isStackable: Boolean
        get() = itemAttributes and ItemAttribute.STACKABLE.value == ItemAttribute.STACKABLE.value

    val isConsumable: Boolean
        get() = itemAttributes and ItemAttribute.CONSUMABLE.value == ItemAttribute.CONSUMABLE.value

    fun isSameItemType(candidateInventoryItem: InventoryItem): Boolean {
        return itemTypeID == candidateInventoryItem.itemTypeID
    }

    //For now, we will set the trade in value of items at about one third their original value
    val tradeValue: Int
        get() =//For now, we will set the trade in value of items at about one third their original value
            if (itemValue >= 0) {
                MathUtils.floor(itemValue * .33f) + 2
            } else {
                0
            }

    val isInventoryItemOffensiveWand: Boolean
        get() = itemUseType and ItemUseType.WAND_ONEHAND.value == ItemUseType.WAND_ONEHAND.value ||
                itemUseType and ItemUseType.WAND_TWOHAND.value == ItemUseType.WAND_TWOHAND.value

    val isInventoryItemOffensive: Boolean
        get() = itemUseType and ItemUseType.WEAPON_ONEHAND.value == ItemUseType.WEAPON_ONEHAND.value || itemUseType and ItemUseType.WEAPON_TWOHAND.value == ItemUseType.WEAPON_TWOHAND.value || itemUseType and ItemUseType.WAND_ONEHAND.value == ItemUseType.WAND_ONEHAND.value || itemUseType and ItemUseType.WAND_TWOHAND.value == ItemUseType.WAND_TWOHAND.value

    val isInventoryItemDefensive: Boolean
        get() = itemUseType and ItemUseType.ARMOR_CHEST.value == ItemUseType.ARMOR_CHEST.value || itemUseType and ItemUseType.ARMOR_HELMET.value == ItemUseType.ARMOR_HELMET.value || itemUseType and ItemUseType.ARMOR_FEET.value == ItemUseType.ARMOR_FEET.value || itemUseType and ItemUseType.ARMOR_SHIELD.value == ItemUseType.ARMOR_SHIELD.value

    companion object {
        @kotlin.jvm.JvmStatic
        fun doesRestoreHP(itemUseType: Int): Boolean {
            return itemUseType and ItemUseType.ITEM_RESTORE_HEALTH.value == ItemUseType.ITEM_RESTORE_HEALTH.value
        }

        @kotlin.jvm.JvmStatic
        fun doesRestoreMP(itemUseType: Int): Boolean {
            return itemUseType and ItemUseType.ITEM_RESTORE_MP.value == ItemUseType.ITEM_RESTORE_MP.value
        }
    }
}