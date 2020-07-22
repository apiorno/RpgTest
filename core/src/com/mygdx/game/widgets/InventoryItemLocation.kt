package com.mygdx.game.widgets

class InventoryItemLocation {
    var locationIndex = 0
    var itemTypeAtLocation: String? = null
    var numberItemsAtLocation = 0
    var itemNameProperty: String? = null

    constructor(locationIndex: Int, itemTypeAtLocation: String?, numberItemsAtLocation: Int, itemNameProperty: String?) {
        this.locationIndex = locationIndex
        this.itemTypeAtLocation = itemTypeAtLocation
        this.numberItemsAtLocation = numberItemsAtLocation
        this.itemNameProperty = itemNameProperty
    }

}