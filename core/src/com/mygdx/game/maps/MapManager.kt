package com.mygdx.game.maps

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.mygdx.game.ecs.Component.MESSAGE
import com.mygdx.game.maps.MapFactory.MapType
import com.mygdx.game.ecs.ComponentObserver
import com.mygdx.game.ecs.Entity
import com.mygdx.game.profile.ProfileManager
import com.mygdx.game.profile.ProfileObserver
import com.mygdx.game.profile.ProfileObserver.ProfileEvent
import com.mygdx.game.sfx.ClockActor.TimeOfDay

class MapManager : ProfileObserver {
    lateinit var camera: Camera
    private var mapChanged = false
    private  var currentMap: Map? = null
    lateinit var player: Entity
    var currentSelectedMapEntity: Entity? = null
    var currentLightMapLayer: MapLayer? = null
    var previousLightMapLayer: MapLayer? = null
    private  var timeOfDay: TimeOfDay? = null
    private var currentLightMapOpacity = 0f
    private var previousLightMapOpacity = 1f
    private var timeOfDayChanged = false
    override fun onNotify(profileManager: ProfileManager, event: ProfileEvent) {
        when (event) {
            ProfileEvent.PROFILE_LOADED -> {
                val currentMap = profileManager.getProperty("currentMapType", String::class.java)
                val mapType: MapType
                mapType = if (currentMap == null || currentMap == "null" || currentMap.isEmpty()) {
                    MapType.TOWN
                } else {
                    MapType.valueOf(currentMap)
                }
                loadMap(mapType)
                val topWorldMapStartPosition = profileManager.getProperty("topWorldMapStartPosition", Vector2::class.java)
                if (topWorldMapStartPosition != null) {
                    MapFactory.getMap(MapType.TOP_WORLD)?.playerStart = topWorldMapStartPosition
                }
                val castleOfDoomMapStartPosition = profileManager.getProperty("castleOfDoomMapStartPosition", Vector2::class.java)
                if (castleOfDoomMapStartPosition != null) {
                    MapFactory.getMap(MapType.CASTLE_OF_DOOM)?.playerStart = castleOfDoomMapStartPosition
                }
                val townMapStartPosition = profileManager.getProperty("townMapStartPosition", Vector2::class.java)
                if (townMapStartPosition != null) {
                    MapFactory.getMap(MapType.TOWN)?.playerStart = townMapStartPosition
                }
            }
            ProfileEvent.SAVING_PROFILE -> {
                profileManager.setProperty("currentMapType", currentMap?.currentMapType.toString())
                profileManager.setProperty("topWorldMapStartPosition", MapFactory.getMap(MapType.TOP_WORLD)?.playerStart!!)
                profileManager.setProperty("castleOfDoomMapStartPosition", MapFactory.getMap(MapType.CASTLE_OF_DOOM)?.playerStart!!)
                profileManager.setProperty("townMapStartPosition", MapFactory.getMap(MapType.TOWN)?.playerStart!!)
            }
            ProfileEvent.CLEAR_CURRENT_PROFILE -> {
                currentMap = null
                profileManager.setProperty("currentMapType", MapType.TOWN.toString())
                MapFactory.clearCache()
                profileManager.setProperty("topWorldMapStartPosition", MapFactory.getMap(MapType.TOP_WORLD)?.playerStart!!)
                profileManager.setProperty("castleOfDoomMapStartPosition", MapFactory.getMap(MapType.CASTLE_OF_DOOM)?.playerStart!!)
                profileManager.setProperty("townMapStartPosition", MapFactory.getMap(MapType.TOWN)?.playerStart!!)
            }
        }
    }

    fun loadMap(mapType: MapType?) {
        val map = MapFactory.getMap(mapType)
        if (map == null) {
            Gdx.app.debug(TAG, "Map does not exist!  ")
            return
        }
        if (currentMap != null) {
            currentMap!!.unloadMusic()
            previousLightMapLayer?.opacity = 0f
            previousLightMapLayer = null
            currentLightMapLayer?.opacity = 1f
            currentLightMapLayer = null
        }
        map.loadMusic()
        currentMap = map
        mapChanged = true
        clearCurrentSelectedMapEntity()
        Gdx.app.debug(TAG, "Player Start: (" + currentMap!!.playerStart.x + "," + currentMap!!.playerStart.y + ")")
    }

    fun unregisterCurrentMapEntityObservers() {
        if (currentMap != null) {
            val entities = currentMap!!.mapEntities
           entities.forEach { it.unregisterObservers() }

            val questEntities = currentMap!!.mapQuestEntities
            questEntities.forEach { it?.unregisterObservers() }
        }
    }

    fun registerCurrentMapEntityObservers(observer: ComponentObserver?) {
        if (currentMap != null) {
            val entities = currentMap!!.mapEntities
            entities.forEach { it.registerObserver(observer) }

            val questEntities = currentMap!!.mapQuestEntities
            questEntities.forEach { it?.registerObserver(observer) }
        }
    }

    fun disableCurrentMapMusic() {
        currentMap?.unloadMusic()
    }

    fun enableCurrentMapMusic() {
        currentMap?.loadMusic()
    }

    fun setClosestStartPositionFromScaledUnits(position: Vector2) {
        currentMap?.setClosestStartPositionFromScaledUnits(position)
    }

    val collisionLayer: MapLayer?
        get() = currentMap?.collisionLayer

    val portalLayer: MapLayer?
        get() = currentMap?.portalLayer

    val questDiscoverLayer: MapLayer?
        get() = currentMap?.questDiscoverLayer

    val enemySpawnLayer: MapLayer?
        get() = currentMap?.enemySpawnLayer

    val currentMapType: MapType?
        get() = currentMap?.currentMapType

    val playerStartUnitScaled: Vector2?
        get() = currentMap?.playerStartUnitScaled

    val currentTiledMap: TiledMap?
        get() {
            currentMap ?: loadMap(MapType.TOWN)
            return currentMap?.currentTiledMap
        }

    fun getQuestItemSpawnPositions(objectName: String?, objectTaskID: String?): Array<Vector2> {
        return currentMap!!.getQuestItemSpawnPositions(objectName, objectTaskID)
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
        currentLightMapLayer = when (timeOfDay) {
            TimeOfDay.DAWN -> currentMap!!.lightMapDawnLayer
            TimeOfDay.AFTERNOON -> currentMap!!.lightMapAfternoonLayer
            TimeOfDay.DUSK -> currentMap!!.lightMapDuskLayer
            TimeOfDay.NIGHT -> currentMap!!.lightMapNightLayer
            else -> currentMap!!.lightMapAfternoonLayer
        }
        if (this.timeOfDayChanged) {
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

    fun updateCurrentMapEntities(mapMgr: MapManager, batch: Batch, delta: Float) {
        currentMap?.updateMapEntities(mapMgr, batch, delta)
    }

    fun updateCurrentMapEffects(mapMgr: MapManager?, batch: Batch, delta: Float) {
        currentMap?.updateMapEffects(mapMgr, batch, delta)
    }

    val currentMapEntities: Array<Entity>?
        get() = currentMap?.mapEntities

    val currentMapQuestEntities: Array<Entity>?
        get() = currentMap?.mapQuestEntities

    fun addMapQuestEntities(entities: Array<Entity?>?) {
        currentMap?.mapQuestEntities?.addAll(entities)
    }

    fun removeMapQuestEntity(entity: Entity) {
        entity.unregisterObservers()
        val positions = ProfileManager.getProperty(entity.entityConfig?.entityID!!, Array::class.java) as Array<Vector2>?
                ?: return
        positions.removeAll { (it.x == entity.currentPosition?.x &&
                it.y == entity.currentPosition?.y) }

        currentMap?.mapQuestEntities?.removeValue(entity, true)
        ProfileManager.setProperty(entity.entityConfig!!.entityID!!, positions)
    }

    fun clearAllMapQuestEntities() {
        currentMap?.mapQuestEntities?.clear()
    }

    fun clearCurrentSelectedMapEntity() {
        currentSelectedMapEntity?.sendMessage(MESSAGE.ENTITY_DESELECTED)
        currentSelectedMapEntity = null
    }

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