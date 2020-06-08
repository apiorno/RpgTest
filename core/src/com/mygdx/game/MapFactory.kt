package com.mygdx.game

import com.badlogic.gdx.maps.tiled.TiledMap
import java.util.*

object MapFactory {
    //All maps for the game
    private val mapTable = Hashtable<MapType, TiledMap>()
    fun getMap(mapType: MapType?): TiledMap? {
        var map: TiledMap? = null
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