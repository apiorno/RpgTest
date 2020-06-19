package com.mygdx.game.ecs

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.GridPoint2
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Json
import com.mygdx.game.maps.MapManager
import com.mygdx.game.Utility
import com.mygdx.game.ecs.Entity.AnimationType
import java.util.*

abstract class GraphicsComponent protected constructor() : ComponentSubject(), Component {
    protected var _currentFrame: TextureRegion? = null
    protected var _frameTime = 0f
    protected var _currentState: Entity.State
    protected var _currentDirection: Entity.Direction
    protected var _json: Json
    var _currentPosition: Vector2
    protected var _animations: Hashtable<AnimationType, Animation<TextureRegion>>
    protected var _shapeRenderer: ShapeRenderer
    abstract fun update(entity: Entity, mapManager: MapManager, batch: Batch, delta: Float)
    protected fun updateAnimations(delta: Float) {
        _frameTime = (_frameTime + delta) % 5 //Want to avoid overflow
        when (_currentDirection) {
            Entity.Direction.DOWN -> if (_currentState == Entity.State.WALKING) {
                val animation = _animations[AnimationType.WALK_DOWN] ?: return
                _currentFrame = animation.getKeyFrame(_frameTime)
            } else if (_currentState == Entity.State.IDLE) {
                val animation = _animations[AnimationType.WALK_DOWN] ?: return
                _currentFrame = animation.keyFrames[0]
            } else if (_currentState == Entity.State.IMMOBILE) {
                val animation = _animations[AnimationType.IMMOBILE] ?: return
                _currentFrame = animation.getKeyFrame(_frameTime)
            }
            Entity.Direction.LEFT -> if (_currentState == Entity.State.WALKING) {
                val animation = _animations[AnimationType.WALK_LEFT] ?: return
                _currentFrame = animation.getKeyFrame(_frameTime)
            } else if (_currentState == Entity.State.IDLE) {
                val animation = _animations[AnimationType.WALK_LEFT] ?: return
                _currentFrame = animation.keyFrames[0]
            } else if (_currentState == Entity.State.IMMOBILE) {
                val animation = _animations[AnimationType.IMMOBILE] ?: return
                _currentFrame = animation.getKeyFrame(_frameTime)
            }
            Entity.Direction.UP -> if (_currentState == Entity.State.WALKING) {
                val animation = _animations[AnimationType.WALK_UP] ?: return
                _currentFrame = animation.getKeyFrame(_frameTime)
            } else if (_currentState == Entity.State.IDLE) {
                val animation = _animations[AnimationType.WALK_UP] ?: return
                _currentFrame = animation.keyFrames[0]
            } else if (_currentState == Entity.State.IMMOBILE) {
                val animation = _animations[AnimationType.IMMOBILE] ?: return
                _currentFrame = animation.getKeyFrame(_frameTime)
            }
            Entity.Direction.RIGHT -> if (_currentState == Entity.State.WALKING) {
                val animation = _animations[AnimationType.WALK_RIGHT] ?: return
                _currentFrame = animation.getKeyFrame(_frameTime)
            } else if (_currentState == Entity.State.IDLE) {
                val animation = _animations[AnimationType.WALK_RIGHT] ?: return
                _currentFrame = animation.keyFrames[0]
            } else if (_currentState == Entity.State.IMMOBILE) {
                val animation = _animations[AnimationType.IMMOBILE] ?: return
                _currentFrame = animation.getKeyFrame(_frameTime)
            }
        }
    }

    //Specific to two frame animations where each frame is stored in a separate texture
    protected fun loadAnimation(firstTexture: String, secondTexture: String, points: com.badlogic.gdx.utils.Array<GridPoint2>, frameDuration: Float): Animation<TextureRegion>? {
        Utility.loadTextureAsset(firstTexture)
        val texture1 = Utility.getTextureAsset(firstTexture)
        Utility.loadTextureAsset(secondTexture)
        val texture2 = Utility.getTextureAsset(secondTexture)
        val texture1Frames = TextureRegion.split(texture1, Entity.Companion.FRAME_WIDTH, Entity.Companion.FRAME_HEIGHT)
        val texture2Frames = TextureRegion.split(texture2, Entity.Companion.FRAME_WIDTH, Entity.Companion.FRAME_HEIGHT)
        val point = points.first()
        val animation: Animation<TextureRegion> = Animation(frameDuration, texture1Frames[point.x][point.y], texture2Frames[point.x][point.y])
        animation.playMode = Animation.PlayMode.LOOP
        return animation
    }

    protected fun loadAnimation(textureName: String, points: com.badlogic.gdx.utils.Array<GridPoint2>, frameDuration: Float): Animation<TextureRegion>? {
        Utility.loadTextureAsset(textureName)
        val texture = Utility.getTextureAsset(textureName)
        val textureFrames = TextureRegion.split(texture, Entity.Companion.FRAME_WIDTH, Entity.Companion.FRAME_HEIGHT)
        val animationKeyFrames = arrayOfNulls<TextureRegion>(points.size)
        for (i in 0 until points.size) {
            animationKeyFrames[i] = textureFrames[points[i].x][points[i].y]
        }
        val animation: Animation<TextureRegion> = Animation(frameDuration, *animationKeyFrames as Array<TextureRegion>)
        animation.playMode = Animation.PlayMode.LOOP
        return animation
    }

    fun getAnimation(type: AnimationType?): Animation<TextureRegion> {
        return _animations[type]!!
    }

    init {
        _currentPosition = Vector2(0F, 0F)
        _currentState = Entity.State.WALKING
        _currentDirection = Entity.Direction.DOWN
        _json = Json()
        _animations = Hashtable()
        _shapeRenderer = ShapeRenderer()
    }
}