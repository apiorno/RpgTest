package com.mygdx.game.maps

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.mygdx.game.maps.MapFactory.MapType
import com.mygdx.game.Utility
import com.mygdx.game.audio.AudioManager
import com.mygdx.game.audio.AudioObserver
import com.mygdx.game.audio.AudioObserver.AudioCommand
import com.mygdx.game.audio.AudioObserver.AudioTypeEvent
import com.mygdx.game.audio.AudioSubject
import com.mygdx.game.ecs.Entity
import com.mygdx.game.sfx.ParticleEffectFactory.ParticleEffectType
import java.util.*

abstract class Map internal constructor(mapType: MapType, fullMapPath: String?) : AudioSubject {
    private val _observers: Array<AudioObserver>
    protected open lateinit var _json: Json
    protected var _playerStartPositionRect: Vector2
    protected var _closestPlayerStartPosition: Vector2
    protected var _convertedUnits: Vector2
    var currentTiledMap: TiledMap? = null
        protected set
    var playerStart: Vector2
    protected lateinit var _npcStartPositions: Array<Vector2>
    protected lateinit var _specialNPCStartPositions: Hashtable<String, Vector2>
    var collisionLayer: MapLayer? = null
        protected set
    var portalLayer: MapLayer? = null
        protected set
    protected var _spawnsLayer: MapLayer? = null
    var questItemSpawnLayer: MapLayer? = null
        protected set
    var questDiscoverLayer: MapLayer? = null
        protected set
    var enemySpawnLayer: MapLayer? = null
        protected set
     var _particleEffectSpawnLayer: MapLayer? = null
    var lightMapDawnLayer: MapLayer? = null
        protected set
    var lightMapAfternoonLayer: MapLayer? = null
        protected set
    var lightMapDuskLayer: MapLayer? = null
        protected set
    var lightMapNightLayer: MapLayer? = null
        protected set
    var currentMapType: MapType
    var mapEntities: Array<Entity>
        protected set
    var mapQuestEntities: Array<Entity?>
        protected set
    var mapParticleEffects: Array<ParticleEffect>
        protected set

    fun getParticleEffectSpawnPositions(particleEffectType: ParticleEffectType): Array<Vector2> {
        val positions = Array<Vector2>()
        for (`object` in _particleEffectSpawnLayer!!.objects) {
            val name = `object`.name
            if (name == null || name.isEmpty() ||
                    !name.equals(particleEffectType.toString(), ignoreCase = true)) {
                continue
            }
            val rect = (`object` as RectangleMapObject).rectangle
            //Get center of rectangle
            var x = rect.getX() + rect.getWidth() / 2
            var y = rect.getY() + rect.getHeight() / 2

            //scale by the unit to convert from map coordinates
            x *= UNIT_SCALE
            y *= UNIT_SCALE
            positions.add(Vector2(x, y))
        }
        return positions
    }

    fun getQuestItemSpawnPositions(objectName: String?, objectTaskID: String?): Array<Vector2> {
        val positions = Array<Vector2>()
        for (`object` in questItemSpawnLayer!!.objects) {
            val name = `object`.name
            val taskID = `object`.properties["taskID"] as String?
            if (name == null || taskID == null ||
                    name.isEmpty() || taskID.isEmpty() ||
                    !name.equals(objectName, ignoreCase = true) ||
                    !taskID.equals(objectTaskID, ignoreCase = true)) {
                continue
            }
            //Get center of rectangle
            var x = (`object` as RectangleMapObject).rectangle.getX()
            var y = `object`.rectangle.getY()

            //scale by the unit to convert from map coordinates
            x *= UNIT_SCALE
            y *= UNIT_SCALE
            positions.add(Vector2(x, y))
        }
        return positions
    }

    fun addMapQuestEntities(entities: Array<Entity?>?) {
        mapQuestEntities.addAll(entities)
    }

    fun updateMapEntities(mapMgr: MapManager, batch: Batch, delta: Float) {
        for (i in 0 until mapEntities.size) {
            mapEntities[i].update(mapMgr, batch, delta)
        }
        for (i in 0 until mapQuestEntities.size) {
            mapQuestEntities[i]!!.update(mapMgr, batch, delta)
        }
    }

    fun updateMapEffects(mapMgr: MapManager?, batch: Batch, delta: Float) {
        for (i in 0 until mapParticleEffects.size) {
            batch.begin()
            mapParticleEffects[i].draw(batch, delta)
            batch.end()
        }
    }

    fun dispose() {
        for (i in 0 until mapEntities.size) {
            mapEntities[i].dispose()
        }
        for (i in 0 until mapQuestEntities.size) {
            mapQuestEntities[i]!!.dispose()
        }
        for (i in 0 until mapParticleEffects.size) {
            mapParticleEffects[i].dispose()
        }
    }

    val playerStartUnitScaled: Vector2
        get() {
            val playerStart = playerStart.cpy()
            playerStart[playerStart.x * UNIT_SCALE] = playerStart.y * UNIT_SCALE
            return playerStart
        }

    //Get center of rectangle

    //scale by the unit to convert from map coordinates
    private val nPCStartPositions: Array<Vector2>
        get() {
            val npcStartPositions = Array<Vector2>()
            for (`object` in _spawnsLayer!!.objects) {
                val objectName = `object`.name
                if (objectName == null || objectName.isEmpty()) {
                    continue
                }
                if (objectName.equals(NPC_START, ignoreCase = true)) {
                    //Get center of rectangle
                    var x = (`object` as RectangleMapObject).rectangle.getX()
                    var y = `object`.rectangle.getY()

                    //scale by the unit to convert from map coordinates
                    x *= UNIT_SCALE
                    y *= UNIT_SCALE
                    npcStartPositions.add(Vector2(x, y))
                }
            }
            return npcStartPositions
        }

    //This is meant for all the special spawn locations, a catch all, so ignore known ones

    //Get center of rectangle

    //scale by the unit to convert from map coordinates
    private val specialNPCStartPositions: Hashtable<String, Vector2>
        get() {
            val specialNPCStartPositions = Hashtable<String, Vector2>()
            for (`object` in _spawnsLayer!!.objects) {
                val objectName = `object`.name
                if (objectName == null || objectName.isEmpty()) {
                    continue
                }

                //This is meant for all the special spawn locations, a catch all, so ignore known ones
                if (objectName.equals(NPC_START, ignoreCase = true) ||
                        objectName.equals(PLAYER_START, ignoreCase = true)) {
                    continue
                }

                //Get center of rectangle
                var x = (`object` as RectangleMapObject).rectangle.getX()
                var y = `object`.rectangle.getY()

                //scale by the unit to convert from map coordinates
                x *= UNIT_SCALE
                y *= UNIT_SCALE
                specialNPCStartPositions[objectName] = Vector2(x, y)
            }
            return specialNPCStartPositions
        }

    private fun setClosestStartPosition(position: Vector2) {
        Gdx.app.debug(TAG, "setClosestStartPosition INPUT: (" + position.x + "," + position.y + ") " + currentMapType.toString())

        //Get last known position on this map
        _playerStartPositionRect[0f] = 0f
        _closestPlayerStartPosition[0f] = 0f
        var shortestDistance = 0f

        //Go through all player start positions and choose closest to last known position
        for (`object` in _spawnsLayer!!.objects) {
            val objectName = `object`.name
            if (objectName == null || objectName.isEmpty()) {
                continue
            }
            if (objectName.equals(PLAYER_START, ignoreCase = true)) {
                (`object` as RectangleMapObject).rectangle.getPosition(_playerStartPositionRect)
                val distance = position.dst2(_playerStartPositionRect)
                Gdx.app.debug(TAG, "DISTANCE: $distance for $currentMapType")
                if (distance < shortestDistance || shortestDistance == 0f) {
                    _closestPlayerStartPosition.set(_playerStartPositionRect)
                    shortestDistance = distance
                    Gdx.app.debug(TAG, "closest START is: (" + _closestPlayerStartPosition.x + "," + _closestPlayerStartPosition.y + ") " + currentMapType.toString())
                }
            }
        }
        playerStart = _closestPlayerStartPosition.cpy()
    }

    fun setClosestStartPositionFromScaledUnits(position: Vector2) {
        if (UNIT_SCALE <= 0) return
        _convertedUnits[position.x / UNIT_SCALE] = position.y / UNIT_SCALE
        setClosestStartPosition(_convertedUnits)
    }

    abstract fun unloadMusic()
    abstract fun loadMusic()
    override fun addObserver(audioObserver: AudioObserver) {
        _observers.add(audioObserver)
    }

    override fun removeObserver(audioObserver: AudioObserver) {
        _observers.removeValue(audioObserver, true)
    }

    override fun removeAllObservers() {
        _observers.removeAll(_observers, true)
    }

    override fun notify(command: AudioCommand, event: AudioTypeEvent) {
        for (observer in _observers) {
            observer.onNotify(command, event)
        }
    }

    companion object {
        private val TAG = Map::class.java.simpleName
        const val UNIT_SCALE = 1 / 16f

        //Map layers
        protected const val COLLISION_LAYER = "MAP_COLLISION_LAYER"
        protected const val SPAWNS_LAYER = "MAP_SPAWNS_LAYER"
        protected const val PORTAL_LAYER = "MAP_PORTAL_LAYER"
        protected const val QUEST_ITEM_SPAWN_LAYER = "MAP_QUEST_ITEM_SPAWN_LAYER"
        protected const val QUEST_DISCOVER_LAYER = "MAP_QUEST_DISCOVER_LAYER"
        protected const val ENEMY_SPAWN_LAYER = "MAP_ENEMY_SPAWN_LAYER"
        protected const val PARTICLE_EFFECT_SPAWN_LAYER = "PARTICLE_EFFECT_SPAWN_LAYER"
        const val BACKGROUND_LAYER = "Background_Layer"
        const val GROUND_LAYER = "Ground_Layer"
        const val DECORATION_LAYER = "Decoration_Layer"
        const val LIGHTMAP_DAWN_LAYER = "MAP_LIGHTMAP_LAYER_DAWN"
        const val LIGHTMAP_AFTERNOON_LAYER = "MAP_LIGHTMAP_LAYER_AFTERNOON"
        const val LIGHTMAP_DUSK_LAYER = "MAP_LIGHTMAP_LAYER_DUSK"
        const val LIGHTMAP_NIGHT_LAYER = "MAP_LIGHTMAP_LAYER_NIGHT"

        //Starting locations
        protected const val PLAYER_START = "PLAYER_START"
        protected const val NPC_START = "NPC_START"
    }

    init {
        _json = Json()
        mapEntities = Array(10)
        _observers = Array()
        mapQuestEntities = Array()
        mapParticleEffects = Array()
        currentMapType = mapType
        playerStart = Vector2(0F, 0F)
        _playerStartPositionRect = Vector2(0F, 0F)
        _closestPlayerStartPosition = Vector2(0F, 0F)
        _convertedUnits = Vector2(0F, 0F)
        run {
            if (fullMapPath == null || fullMapPath.isEmpty()) {
                Gdx.app.debug(TAG, "Map is invalid")
                return@run
            }
            Utility.loadMapAsset(fullMapPath)
            if (Utility.isAssetLoaded(fullMapPath)) {
                currentTiledMap = Utility.getMapAsset(fullMapPath)
            } else {
                Gdx.app.debug(TAG, "Map not loaded")
                return@run
            }
            collisionLayer = currentTiledMap!!.layers[COLLISION_LAYER]
            if (collisionLayer == null) {
                Gdx.app.debug(TAG, "No collision layer!")
            }
            portalLayer = currentTiledMap!!.layers[PORTAL_LAYER]
            if (portalLayer == null) {
                Gdx.app.debug(TAG, "No portal layer!")
            }
            _spawnsLayer = currentTiledMap!!.layers[SPAWNS_LAYER]
            if (_spawnsLayer == null) {
                Gdx.app.debug(TAG, "No spawn layer!")
            } else {
                setClosestStartPosition(playerStart)
            }
            questItemSpawnLayer = currentTiledMap!!.layers[QUEST_ITEM_SPAWN_LAYER]
            if (questItemSpawnLayer == null) {
                Gdx.app.debug(TAG, "No quest item spawn layer!")
            }
            questDiscoverLayer = currentTiledMap!!.layers[QUEST_DISCOVER_LAYER]
            if (questDiscoverLayer == null) {
                Gdx.app.debug(TAG, "No quest discover layer!")
            }
            enemySpawnLayer = currentTiledMap!!.layers[ENEMY_SPAWN_LAYER]
            if (enemySpawnLayer == null) {
                Gdx.app.debug(TAG, "No enemy layer found!")
            }
            lightMapDawnLayer = currentTiledMap!!.layers[LIGHTMAP_DAWN_LAYER]
            if (lightMapDawnLayer == null) {
                Gdx.app.debug(TAG, "No dawn lightmap layer found!")
            }
            lightMapAfternoonLayer = currentTiledMap!!.layers[LIGHTMAP_AFTERNOON_LAYER]
            if (lightMapAfternoonLayer == null) {
                Gdx.app.debug(TAG, "No afternoon lightmap layer found!")
            }
            lightMapDuskLayer = currentTiledMap!!.layers[LIGHTMAP_DUSK_LAYER]
            if (lightMapDuskLayer == null) {
                Gdx.app.debug(TAG, "No dusk lightmap layer found!")
            }
            lightMapNightLayer = currentTiledMap!!.layers[LIGHTMAP_NIGHT_LAYER]
            if (lightMapNightLayer == null) {
                Gdx.app.debug(TAG, "No night lightmap layer found!")
            }
            _particleEffectSpawnLayer = currentTiledMap!!.layers[PARTICLE_EFFECT_SPAWN_LAYER]
            if (_particleEffectSpawnLayer == null) {
                Gdx.app.debug(TAG, "No particle effect spawn layer!")
            }
            _npcStartPositions = nPCStartPositions
            _specialNPCStartPositions = specialNPCStartPositions

            //Observers
            addObserver(AudioManager.instance)
        }
    }
}