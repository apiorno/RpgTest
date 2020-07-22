package com.mygdx.game.maps

import java.util.*

object MapFactory {
    //All maps for the game
    private val mapTable = Hashtable<MapType, Map>()
    fun getMap(mapType: MapType?): Map? {
        var map: Map? = null
        when (mapType) {
            MapType.TOP_WORLD -> {
                map = mapTable[MapType.TOP_WORLD]
                if (map == null) {
                    map = TopWorldMap()
                    mapTable[MapType.TOP_WORLD] = map
                }
            }
            MapType.TOWN -> {
                map = mapTable[MapType.TOWN]
                if (map == null) {
                    map = TownMap()
                    mapTable[MapType.TOWN] = map
                }
            }
            MapType.CASTLE_OF_DOOM -> {
                map = mapTable[MapType.CASTLE_OF_DOOM]
                if (map == null) {
                    map = CastleDoomMap()
                    mapTable[MapType.CASTLE_OF_DOOM] = map
                }
            }
            else -> {
            }
        }
        return map
    }

    fun clearCache() {
        mapTable.values.forEach { it.dispose() }
        mapTable.clear()
    }

    enum class MapType {
        TOP_WORLD, TOWN, CASTLE_OF_DOOM
    }
}