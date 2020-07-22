package com.mygdx.game.ecs

import com.badlogic.gdx.math.Vector2
import com.mygdx.game.ecs.Component.MESSAGE
import com.mygdx.game.maps.MapManager

class NPCPhysicsComponent : PhysicsComponent() {
    private var state: Entity.State? = null
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
                }
                string[0].equals(MESSAGE.CURRENT_STATE.toString(), ignoreCase = true) -> {
                    state = json.fromJson(Entity.State::class.java, string[1])
                }
                string[0].equals(MESSAGE.CURRENT_DIRECTION.toString(), ignoreCase = true) -> {
                    currentDirection = json.fromJson(Entity.Direction::class.java, string[1])
                }
            }
        }
    }

    override fun update(entity: Entity, mapMgr: MapManager, delta: Float) {
        updateBoundingBoxPosition(nextEntityPosition)
        if (isEntityFarFromPlayer(mapMgr)) {
            entity.sendMessage(MESSAGE.ENTITY_DESELECTED)
        }
        if (state == Entity.State.IMMOBILE) return
        if (!isCollisionWithMapLayer(entity, mapMgr) &&
                !isCollisionWithMapEntities(entity, mapMgr) && state == Entity.State.WALKING) {
            setNextPositionToCurrent(entity)
        } else {
            updateBoundingBoxPosition(currentEntityPosition)
        }
        calculateNextPosition(delta)
    }

    private fun isEntityFarFromPlayer(mapMgr: MapManager): Boolean {
        //Check distance
        selectionRay[mapMgr.player.currentBoundingBox!!.x, mapMgr.player.currentBoundingBox!!.y, 0.0f, boundingBox.x, boundingBox.y] = 0.0f
        val distance = selectionRay.origin.dst(selectionRay.direction)
        return distance > selectRayMaximumDistance
    }

    override fun isCollisionWithMapEntities(entity: Entity, mapMgr: MapManager): Boolean {
        //Test against player
        if (isCollision(entity, mapMgr.player)) {
            return true
        }
        return super.isCollisionWithMapEntities(entity, mapMgr)
    }

    companion object {
        private val TAG = NPCPhysicsComponent::class.java.simpleName
    }

    init {
        boundingBoxLocation = BoundingBoxLocation.CENTER
        initBoundingBox(0.4f, 0.15f)
    }
}