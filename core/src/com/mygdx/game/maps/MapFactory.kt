package com.mygdx.game.maps

import java.util.*

object MapFactory {
    //All maps for the game
    private val _mapTable = Hashtable<MapType, Map>()
    fun getMap(mapType: MapType?): Map? {
        var map: Map? = null
        when (mapType) {
            MapType.TOP_WORLD -> {
                map = _mapTable[MapType.TOP_WORLD]
                if (map == null) {
                    map = TopWorldMap()
                    _mapTable[MapType.TOP_WORLD] = map
                }
            }
            MapType.TOWN -> {
                map = _mapTable[MapType.TOWN]
                if (map == null) {
                    map = TownMap()
                    _mapTable[MapType.TOWN] = map
                }
            }
            MapType.CASTLE_OF_DOOM -> {
                map = _mapTable[MapType.CASTLE_OF_DOOM]
                if (map == null) {
                    map = CastleDoomMap()
                    _mapTable[MapType.CASTLE_OF_DOOM] = map
                }
            }
            else -> {
            }
        }
        return map
    }

    @kotlin.jvm.JvmStatic
    fun clearCache() {
        for (map in _mapTable.values) {
            map.dispose()
        }
        _mapTable.clear()
    }

    enum class MapType {
        TOP_WORLD, TOWN, CASTLE_OF_DOOM
    }
}