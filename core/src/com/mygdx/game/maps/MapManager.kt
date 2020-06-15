package com.mygdx.game.maps

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.mygdx.game.EntityConfig
import com.mygdx.game.maps.MapController.Companion.BACKGROUND_LAYER
import com.mygdx.game.maps.MapController.Companion.DECORATION_LAYER
import com.mygdx.game.maps.MapController.Companion.GROUND_LAYER
import com.mygdx.game.maps.MapController.Companion.UNIT_SCALE
import com.mygdx.game.profile.ProfileManager
import com.mygdx.game.profile.ProfileObserver
import com.mygdx.game.sfx.ClockActor.*

open class MapManager : ProfileObserver {
    private var mapRenderer: OrthogonalTiledMapRenderer? = null
    lateinit var camera: OrthographicCamera
    private var mapChanged = false
    private  var currentMapController: MapController? = null
    lateinit var playerConfig: EntityConfig
    var currentSelectedMapEntityConfig: EntityConfig?=null
    var currentLightMapLayer: MapLayer? = null
        private set
    private  var previousLightMapLayer: MapLayer? = null
        private set
    private lateinit var timeOfDay: TimeOfDay
    private var currentLightMapOpacity = 0f
    private var previousLightMapOpacity = 1f
    private var timeOfDayChanged = false
    private val playerStartsByMap = HashMap<MapController,Vector2>()
    init {
        mapRenderer = OrthogonalTiledMapRenderer(currentTiledMap, UNIT_SCALE)
    }
    fun preRenderMap(){
        mapRenderer!!.setView(camera)
        mapRenderer!!.batch.enableBlending()
        mapRenderer!!.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
    }
    fun changeRendererMap(){
        mapRenderer?.map=currentMapController?.currentTiledMap
    }
    fun renderMap(){
        val lightMap = currentLightMapLayer as TiledMapImageLayer?
        val previousLightMap = previousLightMapLayer as TiledMapImageLayer?
        if (lightMap != null) {
            mapRenderer!!.batch.begin()
            val backgroundMapLayer = currentTiledMap!!.layers[BACKGROUND_LAYER] as TiledMapTileLayer?
            if (backgroundMapLayer != null) {
                mapRenderer!!.renderTileLayer(backgroundMapLayer)
            }
            val groundMapLayer = currentTiledMap!!.layers[GROUND_LAYER] as TiledMapTileLayer?
            if (groundMapLayer != null) {
                mapRenderer!!.renderTileLayer(groundMapLayer)
            }
            val decorationMapLayer = currentTiledMap!!.layers[DECORATION_LAYER] as TiledMapTileLayer?
            if (decorationMapLayer != null) {
                mapRenderer!!.renderTileLayer(decorationMapLayer)
            }
            mapRenderer!!.batch.end()
            /*updateCurrentMapEntities(this, mapRenderer!!.batch, delta)
            player.update(this, mapRenderer!!.batch, delta)
            updateCurrentMapEffects(this, mapRenderer!!.batch, delta)*/
            mapRenderer!!.batch.begin()
            mapRenderer!!.batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ONE_MINUS_SRC_ALPHA)
            mapRenderer!!.renderImageLayer(lightMap)
            mapRenderer!!.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
            mapRenderer!!.batch.end()
            if (previousLightMap != null) {
                mapRenderer!!.batch.begin()
                mapRenderer!!.batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ONE_MINUS_SRC_COLOR)
                mapRenderer!!.renderImageLayer(previousLightMap)
                mapRenderer!!.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
                mapRenderer!!.batch.end()
            }
        } else {
            mapRenderer!!.render()
            /*updateCurrentMapEntities(this, mapRenderer!!.batch, delta)
            player!!.update(this, mapRenderer!!.batch, delta)
            updateCurrentMapEffects(this, mapRenderer!!.batch, delta)*/
        }
    }
    fun clearCurrentSelectedMapEntityConfig() {
        if (currentSelectedMapEntityConfig == null) return
        //sendMessage(MESSAGE.ENTITY_DESELECTED)
        currentSelectedMapEntityConfig = null
    }
    fun removeMapQuestEntity(entityConfig: EntityConfig) {
        //entity.unregisterObservers()
        val positions = ProfileManager.instance.getProperty(entityConfig.entityID!!, Array::class.java) as Array<Vector2>
                ?: return
       /* for (position in positions) {
            if (position.x == entity.currentPosition.x &&
                    position.y == entity.currentPosition.y) {
                positions.removeValue(position, true)
                break
            }
        }*/

        //currentMap.getMapQuestEntities().removeValue(entity, true)
        ProfileManager.instance.setProperty(entityConfig.entityID!!, positions)
    }
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
        //val curenntMapPlayerStart = playerStartsByMap[currentMapController!!]!!
        //Gdx.app.debug(TAG, "Player Start: (" + curenntMapPlayerStart.x + "," + curenntMapPlayerStart.y + ")")
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