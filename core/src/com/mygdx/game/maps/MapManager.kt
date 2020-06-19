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
    var camera: Camera? = null
    private var _mapChanged = false
    private var _currentMap: Map? = null
    var player: Entity? = null
    var currentSelectedMapEntity: Entity? = null
    var currentLightMapLayer: MapLayer? = null
        private set
    var previousLightMapLayer: MapLayer? = null
        private set
    private var _timeOfDay: TimeOfDay? = null
    private var _currentLightMapOpacity = 0f
    private var _previousLightMapOpacity = 1f
    private var _timeOfDayChanged = false
    override fun onNotify(profileManager: ProfileManager, event: ProfileEvent) {
        when (event) {
            ProfileEvent.PROFILE_LOADED -> {
                val currentMap = profileManager.getProperty("currentMapType", String::class.java)
                val mapType: MapType
                mapType = if (currentMap == null || currentMap.isEmpty()) {
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
                if (_currentMap != null) {
                    profileManager.setProperty("currentMapType", _currentMap!!.currentMapType.toString())
                }
                profileManager.setProperty("topWorldMapStartPosition", MapFactory.getMap(MapType.TOP_WORLD)?.playerStart!!)
                profileManager.setProperty("castleOfDoomMapStartPosition", MapFactory.getMap(MapType.CASTLE_OF_DOOM)?.playerStart!!)
                profileManager.setProperty("townMapStartPosition", MapFactory.getMap(MapType.TOWN)?.playerStart!!)
            }
            ProfileEvent.CLEAR_CURRENT_PROFILE -> {
                _currentMap = null
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
        if (_currentMap != null) {
            _currentMap!!.unloadMusic()
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
        _currentMap = map
        _mapChanged = true
        clearCurrentSelectedMapEntity()
        Gdx.app.debug(TAG, "Player Start: (" + _currentMap!!.playerStart.x + "," + _currentMap!!.playerStart.y + ")")
    }

    fun unregisterCurrentMapEntityObservers() {
        if (_currentMap != null) {
            val entities = _currentMap!!.mapEntities
            for (entity in entities) {
                entity!!.unregisterObservers()
            }
            val questEntities = _currentMap!!.mapQuestEntities
            for (questEntity in questEntities) {
                questEntity!!.unregisterObservers()
            }
        }
    }

    fun registerCurrentMapEntityObservers(observer: ComponentObserver?) {
        if (_currentMap != null) {
            val entities = _currentMap!!.mapEntities
            for (entity in entities) {
                entity!!.registerObserver(observer)
            }
            val questEntities = _currentMap!!.mapQuestEntities
            for (questEntity in questEntities) {
                questEntity!!.registerObserver(observer)
            }
        }
    }

    fun disableCurrentmapMusic() {
        _currentMap!!.unloadMusic()
    }

    fun enableCurrentmapMusic() {
        _currentMap!!.loadMusic()
    }

    fun setClosestStartPositionFromScaledUnits(position: Vector2) {
        _currentMap!!.setClosestStartPositionFromScaledUnits(position)
    }

    val collisionLayer: MapLayer?
        get() = _currentMap?.collisionLayer

    val portalLayer: MapLayer?
        get() = _currentMap?.portalLayer

    fun getQuestItemSpawnPositions(objectName: String?, objectTaskID: String?): Array<Vector2> {
        return _currentMap!!.getQuestItemSpawnPositions(objectName, objectTaskID)
    }

    val questDiscoverLayer: MapLayer?
        get() = _currentMap!!.questDiscoverLayer

    val enemySpawnLayer: MapLayer?
        get() = _currentMap!!.enemySpawnLayer

    val currentMapType: MapType?
        get() = _currentMap!!.currentMapType

    val playerStartUnitScaled: Vector2?
        get() = _currentMap!!.playerStartUnitScaled

    val currentTiledMap: TiledMap?
        get() {
            if (_currentMap == null) {
                loadMap(MapType.TOWN)
            }
            return _currentMap?.currentTiledMap
        }

    fun updateLightMaps(timeOfDay: TimeOfDay) {
        if (_timeOfDay != timeOfDay) {
            _currentLightMapOpacity = 0f
            _previousLightMapOpacity = 1f
            _timeOfDay = timeOfDay
            _timeOfDayChanged = true
            previousLightMapLayer = currentLightMapLayer
            Gdx.app.debug(TAG, "Time of Day CHANGED")
        }
        when (timeOfDay) {
            TimeOfDay.DAWN -> currentLightMapLayer = _currentMap!!.lightMapDawnLayer
            TimeOfDay.AFTERNOON -> currentLightMapLayer = _currentMap!!.lightMapAfternoonLayer
            TimeOfDay.DUSK -> currentLightMapLayer = _currentMap!!.lightMapDuskLayer
            TimeOfDay.NIGHT -> currentLightMapLayer = _currentMap!!.lightMapNightLayer
            else -> currentLightMapLayer = _currentMap!!.lightMapAfternoonLayer
        }
        if (this._timeOfDayChanged) {
            if (previousLightMapLayer != null && _previousLightMapOpacity != 0f) {
                previousLightMapLayer!!.opacity = _previousLightMapOpacity
                _previousLightMapOpacity -=0.5f
                _previousLightMapOpacity = MathUtils.clamp(_previousLightMapOpacity , 0f, 1f)
                if (_previousLightMapOpacity == 0f) {
                    previousLightMapLayer = null
                }
            }
            if (currentLightMapLayer != null && _currentLightMapOpacity != 1f) {
                currentLightMapLayer!!.opacity = _currentLightMapOpacity
                _currentLightMapOpacity += 0.1f
                _currentLightMapOpacity = MathUtils.clamp(_currentLightMapOpacity, 0f, 1f)
            }
        } else {
            _timeOfDayChanged = false
        }
    }

    fun updateCurrentMapEntities(mapMgr: MapManager, batch: Batch, delta: Float) {
        _currentMap!!.updateMapEntities(mapMgr, batch, delta)
    }

    fun updateCurrentMapEffects(mapMgr: MapManager?, batch: Batch, delta: Float) {
        _currentMap!!.updateMapEffects(mapMgr, batch, delta)
    }

    val currentMapEntities: Array<Entity>?
        get() = _currentMap?.mapEntities

    val currentMapQuestEntities: Array<Entity?>?
        get() = _currentMap?.mapQuestEntities

    fun addMapQuestEntities(entities: Array<Entity?>?) {
        _currentMap?.mapQuestEntities?.addAll(entities)
    }

    fun removeMapQuestEntity(entity: Entity) {
        entity.unregisterObservers()
        val positions = ProfileManager.instance.getProperty(entity.entityConfig?.entityID!!, Array::class.java) as Array<Vector2>?
                ?: return
        for (position in positions) {
            if (position.x == entity.currentPosition?.x &&
                    position.y == entity.currentPosition?.y) {
                positions.removeValue(position, true)
                break
            }
        }
        _currentMap?.mapQuestEntities?.removeValue(entity, true)
        ProfileManager.instance.setProperty(entity.entityConfig!!.entityID!!, positions)
    }

    fun clearAllMapQuestEntities() {
        _currentMap?.mapQuestEntities?.clear()
    }

    fun clearCurrentSelectedMapEntity() {
        if (currentSelectedMapEntity == null) return
        currentSelectedMapEntity!!.sendMessage(MESSAGE.ENTITY_DESELECTED)
        currentSelectedMapEntity = null
    }

    fun hasMapChanged(): Boolean {
        return _mapChanged
    }

    fun setMapChanged(hasMapChanged: Boolean) {
        _mapChanged = hasMapChanged
    }

    companion object {
        private val TAG = MapManager::class.java.simpleName
    }
}