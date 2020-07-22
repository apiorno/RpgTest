package com.mygdx.game.ecs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Ray
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.mygdx.game.ecs.Component.MESSAGE
import com.mygdx.game.maps.Map
import com.mygdx.game.maps.MapManager

abstract class PhysicsComponent internal constructor() : ComponentSubject(), Component {
    abstract fun update(entity: Entity, mapMgr: MapManager, delta: Float)
    protected var nextEntityPosition: Vector2 = Vector2(0F, 0F)
    protected var currentEntityPosition: Vector2 = Vector2(0F, 0F)
    protected var currentDirection: Entity.Direction? = null
    protected var json: Json = Json()
    private var velocity: Vector2 = Vector2(2f, 2f)
    protected var tempEntities: Array<Entity?> = Array()
    var boundingBox: Rectangle = Rectangle()
    protected var boundingBoxLocation: BoundingBoxLocation
    protected var selectionRay: Ray
    protected val selectRayMaximumDistance = 32.0f

    enum class BoundingBoxLocation {
        BOTTOM_LEFT, BOTTOM_CENTER, CENTER
    }

    protected open fun isCollisionWithMapEntities(entity: Entity, mapMgr: MapManager): Boolean {
        tempEntities.clear()
        tempEntities.addAll(mapMgr.currentMapEntities)
        tempEntities.addAll(mapMgr.currentMapQuestEntities)
        var isCollisionWithMapEntities = false
        tempEntities.forEach run@{
            if (it == entity &&boundingBox.overlaps(it.currentBoundingBox)) {
                //Collision
                entity.sendMessage(MESSAGE.COLLISION_WITH_ENTITY)
                isCollisionWithMapEntities = true
                return@run
            }
        }
        tempEntities.clear()
        return isCollisionWithMapEntities
    }

    protected fun isCollision(entitySource: Entity, entityTarget: Entity): Boolean {
        var isCollisionWithMapEntities = false

        if (entitySource != entityTarget && entitySource.currentBoundingBox!!.overlaps(entityTarget.currentBoundingBox)) {
            //Collision
            entitySource.sendMessage(MESSAGE.COLLISION_WITH_ENTITY)
            isCollisionWithMapEntities = true
        }
        return isCollisionWithMapEntities
    }

    protected fun isCollisionWithMapLayer(entity: Entity, mapMgr: MapManager): Boolean {
        val mapCollisionLayer = mapMgr.collisionLayer ?: return false
        mapCollisionLayer.objects.forEach {
            if (it is RectangleMapObject && boundingBox.overlaps(it.rectangle)) {
                    //Collision
                    entity.sendMessage(MESSAGE.COLLISION_WITH_MAP)
                    return true
            }
        }
        return false
    }

    protected fun setNextPositionToCurrent(entity: Entity) {
        currentEntityPosition.x = nextEntityPosition.x
        currentEntityPosition.y = nextEntityPosition.y

        //Gdx.app.debug(TAG, "SETTING Current Position " + entity.getEntityConfig().getEntityID() + ": (" + _currentEntityPosition.x + "," + _currentEntityPosition.y + ")");
        entity.sendMessage(MESSAGE.CURRENT_POSITION, json.toJson(currentEntityPosition))
    }

    protected fun calculateNextPosition(deltaTime: Float) {
        if (currentDirection == null || deltaTime > .7) return
        var testX = currentEntityPosition.x
        var testY = currentEntityPosition.y
        velocity.scl(deltaTime)
        when (currentDirection) {
            Entity.Direction.LEFT -> testX -= velocity.x
            Entity.Direction.RIGHT -> testX += velocity.x
            Entity.Direction.UP -> testY += velocity.y
            Entity.Direction.DOWN -> testY -= velocity.y
            else -> {
            }
        }
        nextEntityPosition.x = testX
        nextEntityPosition.y = testY

        //velocity
        velocity.scl(1 / deltaTime)
    }

    protected fun initBoundingBox(percentageWidthReduced: Float, percentageHeightReduced: Float) {
        //Update the current bounding box
        val width: Float
        val height: Float
        val origWidth: Float = Entity.FRAME_WIDTH.toFloat()
        val origHeight: Float = Entity.FRAME_HEIGHT.toFloat()
        val widthReductionAmount = 1.0f - percentageWidthReduced //.8f for 20% (1 - .20)
        val heightReductionAmount = 1.0f - percentageHeightReduced //.8f for 20% (1 - .20)
        width = if (widthReductionAmount > 0 && widthReductionAmount < 1) {
            Entity.FRAME_WIDTH * widthReductionAmount
        } else {
            Entity.FRAME_WIDTH.toFloat()
        }
        height = if (heightReductionAmount > 0 && heightReductionAmount < 1) {
            Entity.FRAME_HEIGHT * heightReductionAmount
        } else {
            Entity.FRAME_HEIGHT.toFloat()
        }
        if (width == 0f || height == 0f) {
            Gdx.app.debug(TAG, "Width and Height are 0!! $width:$height")
        }

        //Need to account for the unitscale, since the map coordinates will be in pixels
        val minX: Float = nextEntityPosition.x / Map.UNIT_SCALE
        val minY: Float = nextEntityPosition.y / Map.UNIT_SCALE
        boundingBox.setWidth(width)
        boundingBox.setHeight(height)
        when (boundingBoxLocation) {
            BoundingBoxLocation.BOTTOM_LEFT -> boundingBox[minX, minY, width] = height
            BoundingBoxLocation.BOTTOM_CENTER -> boundingBox.setCenter(minX + origWidth / 2, minY + origHeight / 4)
            BoundingBoxLocation.CENTER -> boundingBox.setCenter(minX + origWidth / 2, minY + origHeight / 2)
        }

        //Gdx.app.debug(TAG, "SETTING Bounding Box for " + entity.getEntityConfig().getEntityID() + ": (" + minX + "," + minY + ")  width: " + width + " height: " + height);
    }

    protected fun updateBoundingBoxPosition(position: Vector2) {
        //Need to account for the unitscale, since the map coordinates will be in pixels
        val minX: Float = position.x / Map.UNIT_SCALE
        val minY: Float = position.y / Map.UNIT_SCALE
        when (boundingBoxLocation) {
            BoundingBoxLocation.BOTTOM_LEFT -> boundingBox[minX, minY, boundingBox.getWidth()] = boundingBox.getHeight()
            BoundingBoxLocation.BOTTOM_CENTER -> boundingBox.setCenter(minX + Entity.FRAME_WIDTH / 2, minY + Entity.FRAME_HEIGHT / 4)
            BoundingBoxLocation.CENTER -> boundingBox.setCenter(minX + Entity.FRAME_WIDTH / 2, minY + Entity.FRAME_HEIGHT / 2)
        }

        //Gdx.app.debug(TAG, "SETTING Bounding Box for " + entity.getEntityConfig().getEntityID() + ": (" + minX + "," + minY + ")  width: " + width + " height: " + height);
    }

    companion object {
        private val TAG = PhysicsComponent::class.java.simpleName
    }

    init {
        boundingBoxLocation = BoundingBoxLocation.BOTTOM_LEFT
        selectionRay = Ray(Vector3(), Vector3())
    }
}