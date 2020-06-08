package com.mygdx.game

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
    private  var currentMap: TiledMap? = null
    private  var currentMapType: MapFactory.MapType? = null
    lateinit var player: Entity
    private lateinit var currentSelectedMapEntity: Entity
    lateinit var currentLightMapLayer: MapLayer
        private set
    private lateinit var previousLightMapLayer: MapLayer
        private set
    private lateinit var timeOfDay: TimeOfDay
    private var currentLightMapOpacity = 0f
    private var previousLightMapOpacity = 1f
    private var timeOfDayChanged = false
    private val playerStartsByMap = HashMap<TiledMap,Vector2>()

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
                if (currentMap != null) {
                    profileManager?.setProperty("currentMapType", currentMapType.toString())
                }
                profileManager?.setProperty("topWorldMapStartPosition", playerStartsByMap[MapFactory.getMap(MapFactory.MapType.TOP_WORLD)]!!)
                profileManager?.setProperty("castleOfDoomMapStartPosition", playerStartsByMap[MapFactory.getMap(MapFactory.MapType.CASTLE_OF_DOOM)]!!)
                profileManager?.setProperty("townMapStartPosition", playerStartsByMap[MapFactory.getMap(MapFactory.MapType.TOWN)]!!)
            }
            ProfileObserver.ProfileEvent.CLEAR_CURRENT_PROFILE -> {
                currentMap = null
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
        if (currentMap != null) {
            currentMap!!.unloadMusic()
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
        Gdx.app.debug(TAG, "Player Start: (" + _currentMap.getPlayerStart().x + "," + _currentMap.getPlayerStart().y + ")")
    }

    fun unregisterCurrentMapEntityObservers() {
        if (_currentMap != null) {
            val entities = _currentMap.getMapEntities()
            for (entity in entities!!) {
                entity!!.unregisterObservers()
            }
            val questEntities = _currentMap.getMapQuestEntities()
            for (questEntity in questEntities!!) {
                questEntity!!.unregisterObservers()
            }
        }
    }

    fun registerCurrentMapEntityObservers(observer: ComponentObserver?) {
        if (_currentMap != null) {
            val entities = _currentMap.getMapEntities()
            for (entity in entities!!) {
                entity!!.registerObserver(observer)
            }
            val questEntities = _currentMap.getMapQuestEntities()
            for (questEntity in questEntities!!) {
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
        get() = _currentMap.getCollisionLayer()

    val portalLayer: MapLayer?
        get() = _currentMap.getPortalLayer()

    fun getQuestItemSpawnPositions(objectName: String?, objectTaskID: String?): Array<Vector2?>? {
        return _currentMap!!.getQuestItemSpawnPositions(objectName, objectTaskID)
    }

    val questDiscoverLayer: MapLayer?
        get() = _currentMap.getQuestDiscoverLayer()

    val enemySpawnLayer: MapLayer?
        get() = _currentMap.getEnemySpawnLayer()

    val currentMapType: MapType?
        get() = _currentMap.getCurrentMapType()

    val playerStartUnitScaled: Vector2?
        get() = _currentMap.getPlayerStartUnitScaled()

    val currentTiledMap: TiledMap?
        get() {
            if (_currentMap == null) {
                loadMap(MapType.TOWN)
            }
            return _currentMap.getCurrentTiledMap()
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
            TimeOfDay.DAWN -> currentLightMapLayer = _currentMap.getLightMapDawnLayer()
            TimeOfDay.AFTERNOON -> currentLightMapLayer = _currentMap.getLightMapAfternoonLayer()
            TimeOfDay.DUSK -> currentLightMapLayer = _currentMap.getLightMapDuskLayer()
            TimeOfDay.NIGHT -> currentLightMapLayer = _currentMap.getLightMapNightLayer()
            else -> currentLightMapLayer = _currentMap.getLightMapAfternoonLayer()
        }
        if (_timeOfDayChanged) {
            if (previousLightMapLayer != null && _previousLightMapOpacity != 0f) {
                previousLightMapLayer!!.opacity = _previousLightMapOpacity
                _previousLightMapOpacity = MathUtils.clamp(.05.let { (_previousLightMapOpacity -= it).toFloat(); _previousLightMapOpacity }, 0f, 1f)
                if (_previousLightMapOpacity == 0f) {
                    previousLightMapLayer = null
                }
            }
            if (currentLightMapLayer != null && _currentLightMapOpacity != 1f) {
                currentLightMapLayer!!.opacity = _currentLightMapOpacity
                _currentLightMapOpacity = MathUtils.clamp(.01.let { (_currentLightMapOpacity += it).toFloat(); _currentLightMapOpacity }, 0f, 1f)
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

    val currentMapEntities: Array<Entity?>?
        get() = _currentMap.getMapEntities()

    val currentMapQuestEntities: Array<Entity?>?
        get() = _currentMap.getMapQuestEntities()

    fun addMapQuestEntities(entities: Array<Entity?>?) {
        _currentMap.getMapQuestEntities().addAll(entities)
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
        _currentMap.getMapQuestEntities().removeValue(entity, true)
        ProfileManager.instance.setProperty(entity.entityConfig.entityID, positions)
    }

    fun clearAllMapQuestEntities() {
        _currentMap.getMapQuestEntities().clear()
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