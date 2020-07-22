package com.mygdx.game.ecs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.mygdx.game.ecs.Component.MESSAGE
import com.mygdx.game.maps.MapFactory.MapType
import com.mygdx.game.maps.Map
import com.mygdx.game.maps.MapManager

class PlayerPhysicsComponent : PhysicsComponent() {
    private var state: Entity.State? = null
    private var mouseSelectCoordinates: Vector3
    private var isMouseSelectEnabled = false
    private var previousDiscovery: String
    private var previousEnemySpawn: String
    override fun dispose() {}
    override fun receiveMessage(message: String) {
        //Gdx.app.debug(TAG, "Got message " + message);
        val string: Array<String> = message.split(Component.MESSAGE_TOKEN).toTypedArray()
        if (string.isEmpty()) return

        //Specifically for messages with 1 object payload
        if (string.size == 2) {
            when {
                string[0].equals(MESSAGE.INIT_START_POSITION.toString(), ignoreCase = true) -> {
                    currentEntityPosition = json.fromJson(Vector2::class.java, string[1])
                    nextEntityPosition[currentEntityPosition.x] = currentEntityPosition.y
                    previousDiscovery = ""
                    previousEnemySpawn = "0"
                    notify(previousEnemySpawn, ComponentObserver.ComponentEvent.ENEMY_SPAWN_LOCATION_CHANGED)
                }
                string[0].equals(MESSAGE.CURRENT_STATE.toString(), ignoreCase = true) -> {
                    state = json.fromJson(Entity.State::class.java, string[1])
                }
                string[0].equals(MESSAGE.CURRENT_DIRECTION.toString(), ignoreCase = true) -> {
                    currentDirection = json.fromJson(Entity.Direction::class.java, string[1])
                }
                string[0].equals(MESSAGE.INIT_SELECT_ENTITY.toString(), ignoreCase = true) -> {
                    mouseSelectCoordinates = json.fromJson(Vector3::class.java, string[1])
                    isMouseSelectEnabled = true
                }
            }
        }
    }

    override fun update(entity: Entity, mapMgr: MapManager, delta: Float) {
        //We want the hitbox to be at the feet for a better feel
        updateBoundingBoxPosition(nextEntityPosition)
        updatePortalLayerActivation(mapMgr)
        updateDiscoverLayerActivation(mapMgr)
        updateEnemySpawnLayerActivation(mapMgr)
        if (isMouseSelectEnabled) {
            selectMapEntityCandidate(mapMgr)
            isMouseSelectEnabled = false
        }
        if (!isCollisionWithMapLayer(entity, mapMgr) &&
                !isCollisionWithMapEntities(entity, mapMgr) && state == Entity.State.WALKING) {
            setNextPositionToCurrent(entity)
            val camera = mapMgr.camera
            camera.position[currentEntityPosition.x, currentEntityPosition.y] = 0f
            camera.update()
        } else {
            updateBoundingBoxPosition(currentEntityPosition)
        }
        calculateNextPosition(delta)
    }

    private fun selectMapEntityCandidate(mapMgr: MapManager) {
        tempEntities.clear()
        tempEntities.addAll(mapMgr.currentMapEntities)
        tempEntities.addAll(mapMgr.currentMapQuestEntities)

        //Convert screen coordinates to world coordinates, then to unit scale coordinates
        mapMgr.camera.unproject(mouseSelectCoordinates)
        mouseSelectCoordinates.x /= Map.UNIT_SCALE
        mouseSelectCoordinates.y /= Map.UNIT_SCALE

        //Gdx.app.debug(TAG, "Mouse Coordinates " + "(" + _mouseSelectCoordinates.x + "," + _mouseSelectCoordinates.y + ")");
        tempEntities.forEach {
            //Don't break, reset all entities
            it!!.sendMessage(MESSAGE.ENTITY_DESELECTED)
            val mapEntityBoundingBox = it.currentBoundingBox!!
            //Gdx.app.debug(TAG, "Entity Candidate Location " + "(" + mapEntityBoundingBox.x + "," + mapEntityBoundingBox.y + ")");
            if (it.currentBoundingBox!!.contains(mouseSelectCoordinates.x, mouseSelectCoordinates.y)) {
                //Check distance
                selectionRay[boundingBox.x, boundingBox.y, 0.0f, mapEntityBoundingBox.x, mapEntityBoundingBox.y] = 0.0f
                val distance = selectionRay.origin.dst(selectionRay.direction)
                if (distance <= selectRayMaximumDistance) {
                    //We have a valid entity selection
                    //Picked/Selected
                    Gdx.app.debug(TAG, "Selected Entity! " + it.entityConfig.entityID)
                    it.sendMessage(MESSAGE.ENTITY_SELECTED)
                    notify(json.toJson(it.entityConfig), ComponentObserver.ComponentEvent.LOAD_CONVERSATION)
                }
            }
        }
        tempEntities.clear()
    }

    private fun updateDiscoverLayerActivation(mapMgr: MapManager): Boolean {
        val mapDiscoverLayer = mapMgr.questDiscoverLayer ?: return false
        mapDiscoverLayer.objects.forEach {
            if (it is RectangleMapObject && boundingBox.overlaps(it.rectangle)) {
                val questID = it.getName() ?: return false
                val questTaskID = it.getProperties()["taskID"] as String
                val value = questID + Component.MESSAGE_TOKEN + questTaskID
                if (!previousDiscovery.equals(value, ignoreCase = true)) {
                    previousDiscovery = value
                    notify(json.toJson(value), ComponentObserver.ComponentEvent.QUEST_LOCATION_DISCOVERED)
                    Gdx.app.debug(TAG, "Discover Area Activated")
                }
                return true
            }
        }
        mapDiscoverLayer.objects.forEach {
            if (it is RectangleMapObject && boundingBox.overlaps(it.rectangle)) {
                val questID = it.getName() ?: return false
                val questTaskID = it.getProperties()["taskID"] as String
                val value = questID + Component.MESSAGE_TOKEN + questTaskID
                if (!previousDiscovery.equals(value, ignoreCase = true)) {
                    previousDiscovery =  value
                    notify(json.toJson(value), ComponentObserver.ComponentEvent.QUEST_LOCATION_DISCOVERED)
                    Gdx.app.debug(TAG, "Discover Area Activated")
                }
                return true
            }
        }
        return false
    }

    private fun updateEnemySpawnLayerActivation(mapMgr: MapManager): Boolean {
        val mapEnemySpawnLayer = mapMgr.enemySpawnLayer ?: return false
        mapEnemySpawnLayer.objects.forEach {
            if (it is RectangleMapObject && boundingBox.overlaps(it.rectangle)){
                val enemySpawnID = it.getName() ?: return false
                if (!previousEnemySpawn.equals(enemySpawnID, ignoreCase = true)) {
                    Gdx.app.debug(TAG, "Enemy Spawn Area $enemySpawnID Activated with previous Spawn value: $previousEnemySpawn")
                    previousEnemySpawn =  enemySpawnID
                    notify(enemySpawnID, ComponentObserver.ComponentEvent.ENEMY_SPAWN_LOCATION_CHANGED)
                }
                return true
            }
        }
        //If no collision, reset the value
        if (!previousEnemySpawn.equals(0.toString(), ignoreCase = true)) {
            Gdx.app.debug(TAG, "Enemy Spawn Area RESET with previous value $previousEnemySpawn")
            previousEnemySpawn = 0.toString()
            notify(previousEnemySpawn, ComponentObserver.ComponentEvent.ENEMY_SPAWN_LOCATION_CHANGED)
        }
        return false
    }

    private fun updatePortalLayerActivation(mapMgr: MapManager): Boolean {
        val mapPortalLayer = mapMgr.portalLayer ?: return false
        mapPortalLayer.objects.forEach {
            if(it is RectangleMapObject && boundingBox.overlaps(it.rectangle)){
                val mapName = it.getName() ?: return false
                mapMgr.setClosestStartPositionFromScaledUnits(currentEntityPosition)
                mapMgr.loadMap(MapType.valueOf(mapName))
                currentEntityPosition.x = mapMgr.playerStartUnitScaled!!.x
                currentEntityPosition.y = mapMgr.playerStartUnitScaled!!.y
                nextEntityPosition.x = mapMgr.playerStartUnitScaled!!.x
                nextEntityPosition.y = mapMgr.playerStartUnitScaled!!.y
                Gdx.app.debug(TAG, "Portal Activated")
                return true
            }
        }
        return false
    }

    companion object {
        private val TAG = PlayerPhysicsComponent::class.java.simpleName
    }

    init {
        boundingBoxLocation = BoundingBoxLocation.BOTTOM_CENTER
        initBoundingBox(0.3f, 0.5f)
        previousDiscovery = ""
        previousEnemySpawn = "0"
        mouseSelectCoordinates = Vector3(0F, 0F, 0F)
    }
}