package com.mygdx.game.ecs

import com.badlogic.gdx.math.Vector2
import com.mygdx.game.ecs.Component.MESSAGE
import com.mygdx.game.maps.MapManager

class NPCPhysicsComponent : PhysicsComponent() {
    private var _state: Entity.State? = null
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
            } else if (string[0].equals(MESSAGE.CURRENT_STATE.toString(), ignoreCase = true)) {
                _state = _json.fromJson(Entity.State::class.java, string[1])
            } else if (string[0].equals(MESSAGE.CURRENT_DIRECTION.toString(), ignoreCase = true)) {
                _currentDirection = _json.fromJson(Entity.Direction::class.java, string[1])
            }
        }
    }

    override fun update(entity: Entity, mapMgr: MapManager, delta: Float) {
        updateBoundingBoxPosition(_nextEntityPosition)
        if (isEntityFarFromPlayer(mapMgr)) {
            entity.sendMessage(MESSAGE.ENTITY_DESELECTED)
        }
        if (_state == Entity.State.IMMOBILE) return
        if (!isCollisionWithMapLayer(entity, mapMgr) &&
                !isCollisionWithMapEntities(entity, mapMgr) && _state == Entity.State.WALKING) {
            setNextPositionToCurrent(entity)
        } else {
            updateBoundingBoxPosition(_currentEntityPosition)
        }
        calculateNextPosition(delta)
    }

    private fun isEntityFarFromPlayer(mapMgr: MapManager): Boolean {
        //Check distance
        _selectionRay[mapMgr.player!!.currentBoundingBox!!.x, mapMgr.player!!.currentBoundingBox!!.y, 0.0f, _boundingBox.x, _boundingBox.y] = 0.0f
        val distance = _selectionRay.origin.dst(_selectionRay.direction)
        return distance > _selectRayMaximumDistance
    }

    override fun isCollisionWithMapEntities(entity: Entity, mapMgr: MapManager): Boolean {
        //Test against player
        if (isCollision(entity, mapMgr.player!!)) {
            return true
        }
        return super.isCollisionWithMapEntities(entity, mapMgr)
    }

    companion object {
        private val TAG = NPCPhysicsComponent::class.java.simpleName
    }

    init {
        _boundingBoxLocation = BoundingBoxLocation.CENTER
        initBoundingBox(0.4f, 0.15f)
    }
}