package com.mygdx.game.maps

import java.util.*

object MapFactory {
    //All maps for the game
    private val mapTable = Hashtable<MapType, MapController>()
    fun getMap(mapType: MapType?): MapController? {
        var mapController: MapController? = null
        when (mapType) {
            MapType.TOP_WORLD -> {
                mapController = mapTable[MapType.TOP_WORLD]
                if (mapController == null) {
                    mapController = MapController(TopWorldMapContentController())
                    mapTable[MapType.TOP_WORLD] = mapController
                }
            }
            MapType.TOWN -> {
                mapController = mapTable[MapType.TOWN]
                if (mapController == null) {
                    mapController = MapController(TownMapContentController())
                    mapTable[MapType.TOWN] = mapController
                }
            }
            MapType.CASTLE_OF_DOOM -> {
                mapController = mapTable[MapType.CASTLE_OF_DOOM]
                if (mapController == null) {
                    mapController = MapController(CastleDoomMapContentController())
                    mapTable[MapType.CASTLE_OF_DOOM] = mapController
                }
            }
            else -> {
            }
        }
        return mapController
    }

    @kotlin.jvm.JvmStatic
    fun clearCache() {
        for (map in mapTable.values) {
            map.dispose()
        }
        mapTable.clear()
    }

    enum class MapType {
        TOP_WORLD, TOWN, CASTLE_OF_DOOM
    }
}