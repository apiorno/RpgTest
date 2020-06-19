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
    protected var _nextEntityPosition: Vector2
    protected var _currentEntityPosition: Vector2
    protected var _currentDirection: Entity.Direction? = null
    protected var _json: Json
    protected var _velocity: Vector2
    protected var _tempEntities: Array<Entity?>
    var _boundingBox: Rectangle
    protected var _boundingBoxLocation: BoundingBoxLocation
    protected var _selectionRay: Ray
    protected val _selectRayMaximumDistance = 32.0f

    enum class BoundingBoxLocation {
        BOTTOM_LEFT, BOTTOM_CENTER, CENTER
    }

    protected open fun isCollisionWithMapEntities(entity: Entity, mapMgr: MapManager): Boolean {
        _tempEntities.clear()
        _tempEntities.addAll(mapMgr.currentMapEntities)
        _tempEntities.addAll(mapMgr.currentMapQuestEntities)
        var isCollisionWithMapEntities = false
        for (mapEntity in _tempEntities) {
            //Check for testing against self
            if (mapEntity == entity) {
                continue
            }
            val targetRect = mapEntity!!.currentBoundingBox
            if (_boundingBox.overlaps(targetRect)) {
                //Collision
                entity.sendMessage(MESSAGE.COLLISION_WITH_ENTITY)
                isCollisionWithMapEntities = true
                break
            }
        }
        _tempEntities.clear()
        return isCollisionWithMapEntities
    }

    protected fun isCollision(entitySource: Entity, entityTarget: Entity): Boolean {
        var isCollisionWithMapEntities = false
        if (entitySource == entityTarget) {
            return false
        }
        if (entitySource.currentBoundingBox!!.overlaps(entityTarget.currentBoundingBox)) {
            //Collision
            entitySource.sendMessage(MESSAGE.COLLISION_WITH_ENTITY)
            isCollisionWithMapEntities = true
        }
        return isCollisionWithMapEntities
    }

    protected fun isCollisionWithMapLayer(entity: Entity, mapMgr: MapManager): Boolean {
        val mapCollisionLayer = mapMgr.collisionLayer ?: return false
        var rectangle: Rectangle?
        for (`object` in mapCollisionLayer.objects) {
            if (`object` is RectangleMapObject) {
                rectangle = `object`.rectangle
                if (_boundingBox.overlaps(rectangle)) {
                    //Collision
                    entity.sendMessage(MESSAGE.COLLISION_WITH_MAP)
                    return true
                }
            }
        }
        return false
    }

    protected fun setNextPositionToCurrent(entity: Entity) {
        _currentEntityPosition.x = _nextEntityPosition.x
        _currentEntityPosition.y = _nextEntityPosition.y

        //Gdx.app.debug(TAG, "SETTING Current Position " + entity.getEntityConfig().getEntityID() + ": (" + _currentEntityPosition.x + "," + _currentEntityPosition.y + ")");
        entity.sendMessage(MESSAGE.CURRENT_POSITION, _json.toJson(_currentEntityPosition))
    }

    protected fun calculateNextPosition(deltaTime: Float) {
        if (_currentDirection == null) return
        if (deltaTime > .7) return
        var testX = _currentEntityPosition.x
        var testY = _currentEntityPosition.y
        _velocity.scl(deltaTime)
        when (_currentDirection) {
            Entity.Direction.LEFT -> testX -= _velocity.x
            Entity.Direction.RIGHT -> testX += _velocity.x
            Entity.Direction.UP -> testY += _velocity.y
            Entity.Direction.DOWN -> testY -= _velocity.y
            else -> {
            }
        }
        _nextEntityPosition.x = testX
        _nextEntityPosition.y = testY

        //velocity
        _velocity.scl(1 / deltaTime)
    }

    protected fun initBoundingBox(percentageWidthReduced: Float, percentageHeightReduced: Float) {
        //Update the current bounding box
        val width: Float
        val height: Float
        val origWidth: Float = Entity.Companion.FRAME_WIDTH.toFloat()
        val origHeight: Float = Entity.Companion.FRAME_HEIGHT.toFloat()
        val widthReductionAmount = 1.0f - percentageWidthReduced //.8f for 20% (1 - .20)
        val heightReductionAmount = 1.0f - percentageHeightReduced //.8f for 20% (1 - .20)
        width = if (widthReductionAmount > 0 && widthReductionAmount < 1) {
            Entity.Companion.FRAME_WIDTH * widthReductionAmount
        } else {
            Entity.Companion.FRAME_WIDTH.toFloat()
        }
        height = if (heightReductionAmount > 0 && heightReductionAmount < 1) {
            Entity.Companion.FRAME_HEIGHT * heightReductionAmount
        } else {
            Entity.Companion.FRAME_HEIGHT.toFloat()
        }
        if (width == 0f || height == 0f) {
            Gdx.app.debug(TAG, "Width and Height are 0!! $width:$height")
        }

        //Need to account for the unitscale, since the map coordinates will be in pixels
        val minX: Float
        val minY: Float
        if (Map.Companion.UNIT_SCALE > 0) {
            minX = _nextEntityPosition.x / Map.Companion.UNIT_SCALE
            minY = _nextEntityPosition.y / Map.Companion.UNIT_SCALE
        } else {
            minX = _nextEntityPosition.x
            minY = _nextEntityPosition.y
        }
        _boundingBox.setWidth(width)
        _boundingBox.setHeight(height)
        when (_boundingBoxLocation) {
            BoundingBoxLocation.BOTTOM_LEFT -> _boundingBox[minX, minY, width] = height
            BoundingBoxLocation.BOTTOM_CENTER -> _boundingBox.setCenter(minX + origWidth / 2, minY + origHeight / 4)
            BoundingBoxLocation.CENTER -> _boundingBox.setCenter(minX + origWidth / 2, minY + origHeight / 2)
        }

        //Gdx.app.debug(TAG, "SETTING Bounding Box for " + entity.getEntityConfig().getEntityID() + ": (" + minX + "," + minY + ")  width: " + width + " height: " + height);
    }

    protected fun updateBoundingBoxPosition(position: Vector2) {
        //Need to account for the unitscale, since the map coordinates will be in pixels
        val minX: Float
        val minY: Float
        if (Map.Companion.UNIT_SCALE > 0) {
            minX = position.x / Map.Companion.UNIT_SCALE
            minY = position.y / Map.Companion.UNIT_SCALE
        } else {
            minX = position.x
            minY = position.y
        }
        when (_boundingBoxLocation) {
            BoundingBoxLocation.BOTTOM_LEFT -> _boundingBox[minX, minY, _boundingBox.getWidth()] = _boundingBox.getHeight()
            BoundingBoxLocation.BOTTOM_CENTER -> _boundingBox.setCenter(minX + Entity.Companion.FRAME_WIDTH / 2, minY + Entity.Companion.FRAME_HEIGHT / 4)
            BoundingBoxLocation.CENTER -> _boundingBox.setCenter(minX + Entity.Companion.FRAME_WIDTH / 2, minY + Entity.Companion.FRAME_HEIGHT / 2)
        }

        //Gdx.app.debug(TAG, "SETTING Bounding Box for " + entity.getEntityConfig().getEntityID() + ": (" + minX + "," + minY + ")  width: " + width + " height: " + height);
    }

    companion object {
        private val TAG = PhysicsComponent::class.java.simpleName
    }

    init {
        _nextEntityPosition = Vector2(0F, 0F)
        _currentEntityPosition = Vector2(0F, 0F)
        _velocity = Vector2(2f, 2f)
        _boundingBox = Rectangle()
        _json = Json()
        _tempEntities = Array()
        _boundingBoxLocation = BoundingBoxLocation.BOTTOM_LEFT
        _selectionRay = Ray(Vector3(), Vector3())
    }
}