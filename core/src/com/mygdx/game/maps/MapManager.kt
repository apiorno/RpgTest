package com.mygdx.game.maps

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.mygdx.game.profile.ProfileManager
import com.mygdx.game.profile.ProfileObserver
import com.mygdx.game.sfx.ClockActor.*

class MapManager : ProfileObserver {
    lateinit var camera: Camera
    private var mapChanged = false
    private  var currentMapController: MapController? = null
    lateinit var player: Entity
    private lateinit var currentSelectedMapEntity: Entity
    var currentLightMapLayer: MapLayer? = null
        private set
    private  var previousLightMapLayer: MapLayer? = null
        private set
    private lateinit var timeOfDay: TimeOfDay
    private var currentLightMapOpacity = 0f
    private var previousLightMapOpacity = 1f
    private var timeOfDayChanged = false
    private val playerStartsByMap = HashMap<MapController,Vector2>()

    override fun onNotify(profileManager: ProfileManager?, event: ProfileObserver.ProfileEvent?) {
        when (event) {
            ProfileObserver.ProfileEvent.PROFILE_LOADED -> {
                val currentMap = profileManager?.getProperty("currentMapType", String::class.java)
                val mapType: MapFactory.MapType
                mapType = if (currentMap == null || currentMap.isEmpty()) {
                    MapFactory.MapType.TOWN
                } else {
                    MapFactory.MapType.valueOf(currentMap)
                }
                loadMap(mapType)
                val topWorldMapStartPosition = profileManager?.getProperty("topWorldMapStartPosition", Vector2::class.java)
                if (topWorldMapStartPosition != null) {
                   playerStartsByMap[ MapFactory.getMap(MapFactory.MapType.TOP_WORLD)!!]= topWorldMapStartPosition
                }
                val castleOfDoomMapStartPosition = profileManager?.getProperty("castleOfDoomMapStartPosition", Vector2::class.java)
                if (castleOfDoomMapStartPosition != null) {
                    playerStartsByMap[ MapFactory.getMap(MapFactory.MapType.CASTLE_OF_DOOM)!!]= castleOfDoomMapStartPosition
                }
                val townMapStartPosition = profileManager?.getProperty("townMapStartPosition", Vector2::class.java)
                if (townMapStartPosition != null) {
                    playerStartsByMap[ MapFactory.getMap(MapFactory.MapType.TOWN)!!] = townMapStartPosition
                }
            }
            ProfileObserver.ProfileEvent.SAVING_PROFILE -> {
                if (currentMapController != null) {
                    profileManager?.setProperty("currentMapType", currentMapType.toString())
                }
                profileManager?.setProperty("topWorldMapStartPosition", playerStartsByMap[MapFactory.getMap(MapFactory.MapType.TOP_WORLD)]!!)
                profileManager?.setProperty("castleOfDoomMapStartPosition", playerStartsByMap[MapFactory.getMap(MapFactory.MapType.CASTLE_OF_DOOM)]!!)
                profileManager?.setProperty("townMapStartPosition", playerStartsByMap[MapFactory.getMap(MapFactory.MapType.TOWN)]!!)
            }
            ProfileObserver.ProfileEvent.CLEAR_CURRENT_PROFILE -> {
                currentMapController = null
                profileManager?.setProperty("currentMapType", MapFactory.MapType.TOWN.toString())
                MapFactory.clearCache()
                profileManager?.setProperty("topWorldMapStartPosition", playerStartsByMap[MapFactory.getMap(MapFactory.MapType.TOP_WORLD)]!!)
                profileManager?.setProperty("castleOfDoomMapStartPosition", playerStartsByMap[MapFactory.getMap(MapFactory.MapType.CASTLE_OF_DOOM)]!!)
                profileManager?.setProperty("townMapStartPosition", playerStartsByMap[MapFactory.getMap(MapFactory.MapType.TOWN)]!!)
            }
            else -> {
            }
        }
    }

    fun loadMap(mapType: MapFactory.MapType?) {
        val map = MapFactory.getMap(mapType)
        if (map == null) {
            Gdx.app.debug(TAG, "Map does not exist!  ")
            return
        }
        if (currentMapController != null) {
            currentMapController!!.unloadMusic()
            if (previousLightMapLayer != null) {
                previousLightMapLayer!!.opacity = 0f
                previousLightMapLayer = null
            }
            if (currentLightMapLayer != null) {
                currentLightMapLayer!!.opacity = 1f
                currentLightMapLayer = null
            }
        }
        map.loadMusic()
        currentMapController = map
        mapChanged = true
        //clearCurrentSelectedMapEntity()
        val curenntMapPlayerStart = playerStartsByMap[currentMapController]!!
        Gdx.app.debug(TAG, "Player Start: (" + curenntMapPlayerStart.x + "," + curenntMapPlayerStart.y + ")")
    }

    fun disableCurrentmapMusic() {
        currentMapController!!.unloadMusic()
    }

    fun enableCurrentmapMusic() {
        currentMapController!!.loadMusic()
    }

    fun setClosestStartPositionFromScaledUnits(position: Vector2) {
        currentMapController!!.setClosestStartPositionFromScaledUnits(position)
    }

    val collisionLayer: MapLayer?
        get() = currentMapController?.collisionLayer

    val portalLayer: MapLayer?
        get() = currentMapController?.portalLayer

    fun getQuestItemSpawnPositions(objectName: String?, objectTaskID: String?): Array<Vector2> {
        return currentMapController!!.getQuestItemSpawnPositions(objectName, objectTaskID)
    }

    val questDiscoverLayer: MapLayer?
        get() = currentMapController?.questDiscoverLayer

    val enemySpawnLayer: MapLayer?
        get() = currentMapController?.enemySpawnLayer

    val currentMapType: MapFactory.MapType?
        get() = currentMapController?.currentMapType

    val playerStartUnitScaled: Vector2?
        get() = currentMapController?.playerStartUnitScaled

    val currentTiledMap: TiledMap?
        get() {
            if (currentMapController == null) {
                loadMap(MapFactory.MapType.TOWN)
            }
            return currentMapController?.currentTiledMap
        }

    fun updateLightMaps(timeOfDay: TimeOfDay) {
        if (this.timeOfDay != timeOfDay) {
            currentLightMapOpacity = 0f
            previousLightMapOpacity = 1f
            this.timeOfDay = timeOfDay
            timeOfDayChanged = true
            previousLightMapLayer = currentLightMapLayer
            Gdx.app.debug(TAG, "Time of Day CHANGED")
        }
        when (timeOfDay) {
            TimeOfDay.DAWN -> currentLightMapLayer = currentMapController?.lightMapDawnLayer
            TimeOfDay.AFTERNOON -> currentLightMapLayer = currentMapController?.lightMapAfternoonLayer
            TimeOfDay.DUSK -> currentLightMapLayer = currentMapController?.lightMapDuskLayer
            TimeOfDay.NIGHT -> currentLightMapLayer = currentMapController?.lightMapNightLayer
            else -> currentLightMapLayer = currentMapController?.lightMapAfternoonLayer
        }
        if (timeOfDayChanged) {
            if (previousLightMapLayer != null && previousLightMapOpacity != 0f) {
                previousLightMapLayer!!.opacity = previousLightMapOpacity
                previousLightMapOpacity -=0.5f
                previousLightMapOpacity = MathUtils.clamp(previousLightMapOpacity , 0f, 1f)
                if (previousLightMapOpacity == 0f) {
                    previousLightMapLayer = null
                }
            }
            if (currentLightMapLayer != null && currentLightMapOpacity != 1f) {
                currentLightMapLayer!!.opacity = currentLightMapOpacity
                currentLightMapOpacity += 0.1f
                currentLightMapOpacity = MathUtils.clamp(currentLightMapOpacity, 0f, 1f)
            }
        } else {
            timeOfDayChanged = false
        }
    }

    /*fun updateCurrentMapEntities(mapMgr: MapManager, batch: Batch, delta: Float) {
        currentMapController!!.updateMapEntities(mapMgr, batch, delta)
    }

    fun updateCurrentMapEffects(mapMgr: MapManager?, batch: Batch, delta: Float) {
        currentMapController!!.updateMapEffects(mapMgr, batch, delta)
    }

    val currentMapEntities: Array<Entity?>?
        get() = currentMapController.getMapEntities()

    val currentMapQuestEntities: Array<Entity?>?
        get() = currentMapController.getMapQuestEntities()

    fun addMapQuestEntities(entities: Array<Entity?>?) {
        currentMapController.getMapQuestEntities().addAll(entities)
    }

    fun removeMapQuestEntity(entity: Entity) {
        entity.unregisterObservers()
        val positions = ProfileManager.instance.getProperty(entity.entityConfig.entityID, Array::class.java)
                ?: return
        for (position in positions) {
            if (position.x == entity.currentPosition.x &&
                    position.y == entity.currentPosition.y) {
                positions.removeValue(position, true)
                break
            }
        }
        currentMapController.getMapQuestEntities().removeValue(entity, true)
        ProfileManager.instance.setProperty(entity.entityConfig.entityID, positions)
    }

    fun clearAllMapQuestEntities() {
        currentMapController.getMapQuestEntities().clear()
    }

    fun clearCurrentSelectedMapEntity() {
        if (currentSelectedMapEntity == null) return
        currentSelectedMapEntity!!.sendMessage(MESSAGE.ENTITY_DESELECTED)
        currentSelectedMapEntity = null
    }*/

    fun hasMapChanged(): Boolean {
        return mapChanged
    }

    fun setMapChanged(hasMapChanged: Boolean) {
        mapChanged = hasMapChanged
    }

    companion object {
        private val TAG = MapManager::class.java.simpleName
    }


}