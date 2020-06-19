package com.mygdx.game.ecs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.mygdx.game.ecs.Component.MESSAGE
import com.mygdx.game.maps.MapFactory.MapType
import com.mygdx.game.maps.Map
import com.mygdx.game.maps.MapManager

class PlayerPhysicsComponent : PhysicsComponent() {
    private var _state: Entity.State? = null
    private var _mouseSelectCoordinates: Vector3
    private var _isMouseSelectEnabled = false
    private var _previousDiscovery: String
    private var _previousEnemySpawn: String
    override fun dispose() {}
    override fun receiveMessage(message: String) {
        //Gdx.app.debug(TAG, "Got message " + message);
        val string: Array<String> = message.split(Component.Companion.MESSAGE_TOKEN).toTypedArray()
        if (string.size == 0) return

        //Specifically for messages with 1 object payload
        if (string.size == 2) {
            if (string[0].equals(MESSAGE.INIT_START_POSITION.toString(), ignoreCase = true)) {
                _currentEntityPosition = _json.fromJson(Vector2::class.java, string[1])
                _nextEntityPosition[_currentEntityPosition.x] = _currentEntityPosition.y
                _previousDiscovery = ""
                _previousEnemySpawn = "0"
                notify(_previousEnemySpawn, ComponentObserver.ComponentEvent.ENEMY_SPAWN_LOCATION_CHANGED)
            } else if (string[0].equals(MESSAGE.CURRENT_STATE.toString(), ignoreCase = true)) {
                _state = _json.fromJson(Entity.State::class.java, string[1])
            } else if (string[0].equals(MESSAGE.CURRENT_DIRECTION.toString(), ignoreCase = true)) {
                _currentDirection = _json.fromJson(Entity.Direction::class.java, string[1])
            } else if (string[0].equals(MESSAGE.INIT_SELECT_ENTITY.toString(), ignoreCase = true)) {
                _mouseSelectCoordinates = _json.fromJson(Vector3::class.java, string[1])
                _isMouseSelectEnabled = true
            }
        }
    }

    override fun update(entity: Entity, mapMgr: MapManager, delta: Float) {
        //We want the hitbox to be at the feet for a better feel
        updateBoundingBoxPosition(_nextEntityPosition)
        updatePortalLayerActivation(mapMgr)
        updateDiscoverLayerActivation(mapMgr)
        updateEnemySpawnLayerActivation(mapMgr)
        if (_isMouseSelectEnabled) {
            selectMapEntityCandidate(mapMgr)
            _isMouseSelectEnabled = false
        }
        if (!isCollisionWithMapLayer(entity, mapMgr) &&
                !isCollisionWithMapEntities(entity, mapMgr) && _state == Entity.State.WALKING) {
            setNextPositionToCurrent(entity)
            val camera = mapMgr.camera
            camera!!.position[_currentEntityPosition.x, _currentEntityPosition.y] = 0f
            camera.update()
        } else {
            updateBoundingBoxPosition(_currentEntityPosition)
        }
        calculateNextPosition(delta)
    }

    private fun selectMapEntityCandidate(mapMgr: MapManager) {
        _tempEntities.clear()
        _tempEntities.addAll(mapMgr.currentMapEntities)
        _tempEntities.addAll(mapMgr.currentMapQuestEntities)

        //Convert screen coordinates to world coordinates, then to unit scale coordinates
        mapMgr.camera!!.unproject(_mouseSelectCoordinates)
        _mouseSelectCoordinates.x /= Map.Companion.UNIT_SCALE
        _mouseSelectCoordinates.y /= Map.Companion.UNIT_SCALE

        //Gdx.app.debug(TAG, "Mouse Coordinates " + "(" + _mouseSelectCoordinates.x + "," + _mouseSelectCoordinates.y + ")");
        for (mapEntity in _tempEntities) {
            //Don't break, reset all entities
            mapEntity!!.sendMessage(MESSAGE.ENTITY_DESELECTED)
            val mapEntityBoundingBox = mapEntity.currentBoundingBox!!
            //Gdx.app.debug(TAG, "Entity Candidate Location " + "(" + mapEntityBoundingBox.x + "," + mapEntityBoundingBox.y + ")");
            if (mapEntity.currentBoundingBox!!.contains(_mouseSelectCoordinates.x, _mouseSelectCoordinates.y)) {
                //Check distance
                _selectionRay[_boundingBox.x, _boundingBox.y, 0.0f, mapEntityBoundingBox.x, mapEntityBoundingBox.y] = 0.0f
                val distance = _selectionRay.origin.dst(_selectionRay.direction)
                if (distance <= _selectRayMaximumDistance) {
                    //We have a valid entity selection
                    //Picked/Selected
                    Gdx.app.debug(TAG, "Selected Entity! " + mapEntity.entityConfig!!.entityID)
                    mapEntity.sendMessage(MESSAGE.ENTITY_SELECTED)
                    notify(_json.toJson(mapEntity.entityConfig), ComponentObserver.ComponentEvent.LOAD_CONVERSATION)
                }
            }
        }
        _tempEntities.clear()
    }

    private fun updateDiscoverLayerActivation(mapMgr: MapManager): Boolean {
        val mapDiscoverLayer = mapMgr.questDiscoverLayer ?: return false
        var rectangle: Rectangle?
        for (`object` in mapDiscoverLayer.objects) {
            if (`object` is RectangleMapObject) {
                rectangle = `object`.rectangle
                if (_boundingBox.overlaps(rectangle)) {
                    val questID = `object`.getName()
                    val questTaskID = `object`.getProperties()["taskID"] as String
                    val `val` = questID + Component.Companion.MESSAGE_TOKEN + questTaskID
                    if (questID == null) {
                        return false
                    }
                    _previousDiscovery = if (_previousDiscovery.equals(`val`, ignoreCase = true)) {
                        return true
                    } else {
                        `val`
                    }
                    notify(_json.toJson(`val`), ComponentObserver.ComponentEvent.QUEST_LOCATION_DISCOVERED)
                    Gdx.app.debug(TAG, "Discover Area Activated")
                    return true
                }
            }
        }
        return false
    }

    private fun updateEnemySpawnLayerActivation(mapMgr: MapManager): Boolean {
        val mapEnemySpawnLayer = mapMgr.enemySpawnLayer ?: return false
        var rectangle: Rectangle?
        for (`object` in mapEnemySpawnLayer.objects) {
            if (`object` is RectangleMapObject) {
                rectangle = `object`.rectangle
                if (_boundingBox.overlaps(rectangle)) {
                    val enemySpawnID = `object`.getName() ?: return false
                    _previousEnemySpawn = if (_previousEnemySpawn.equals(enemySpawnID, ignoreCase = true)) {
                        //Gdx.app.debug(TAG, "Enemy Spawn Area already activated " + enemySpawnID);
                        return true
                    } else {
                        Gdx.app.debug(TAG, "Enemy Spawn Area $enemySpawnID Activated with previous Spawn value: $_previousEnemySpawn")
                        enemySpawnID
                    }
                    notify(enemySpawnID, ComponentObserver.ComponentEvent.ENEMY_SPAWN_LOCATION_CHANGED)
                    return true
                }
            }
        }

        //If no collision, reset the value
        if (!_previousEnemySpawn.equals(0.toString(), ignoreCase = true)) {
            Gdx.app.debug(TAG, "Enemy Spawn Area RESET with previous value $_previousEnemySpawn")
            _previousEnemySpawn = 0.toString()
            notify(_previousEnemySpawn, ComponentObserver.ComponentEvent.ENEMY_SPAWN_LOCATION_CHANGED)
        }
        return false
    }

    private fun updatePortalLayerActivation(mapMgr: MapManager): Boolean {
        val mapPortalLayer = mapMgr.portalLayer
                ?: //Gdx.app.debug(TAG, "Portal Layer doesn't exist!");
                return false
        var rectangle: Rectangle?
        for (`object` in mapPortalLayer.objects) {
            if (`object` is RectangleMapObject) {
                rectangle = `object`.rectangle
                if (_boundingBox.overlaps(rectangle)) {
                    val mapName = `object`.getName() ?: return false
                    mapMgr.setClosestStartPositionFromScaledUnits(_currentEntityPosition)
                    mapMgr.loadMap(MapType.valueOf(mapName))
                    _currentEntityPosition.x = mapMgr.playerStartUnitScaled!!.x
                    _currentEntityPosition.y = mapMgr.playerStartUnitScaled!!.y
                    _nextEntityPosition.x = mapMgr.playerStartUnitScaled!!.x
                    _nextEntityPosition.y = mapMgr.playerStartUnitScaled!!.y
                    Gdx.app.debug(TAG, "Portal Activated")
                    return true
                }
            }
        }
        return false
    }

    companion object {
        private val TAG = PlayerPhysicsComponent::class.java.simpleName
    }

    init {
        _boundingBoxLocation = BoundingBoxLocation.BOTTOM_CENTER
        initBoundingBox(0.3f, 0.5f)
        _previousDiscovery = ""
        _previousEnemySpawn = "0"
        _mouseSelectCoordinates = Vector3(0F, 0F, 0F)
    }
}