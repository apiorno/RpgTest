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
    protected var currentFrame: TextureRegion? = null
    private var frameTime = 0f
    protected var currentState: Entity.State = Entity.State.WALKING
    protected var currentDirection: Entity.Direction = Entity.Direction.DOWN
    protected var json: Json = Json()
    var currentPosition: Vector2 = Vector2(0F, 0F)
    protected var animations: Hashtable<AnimationType, Animation<TextureRegion>> = Hashtable()
    protected var shapeRenderer: ShapeRenderer = ShapeRenderer()
    abstract fun update(entity: Entity, mapManager: MapManager, batch: Batch, delta: Float)
    protected fun updateAnimations(delta: Float) {
        frameTime = (frameTime + delta) % 5 //Want to avoid overflow
        when (currentDirection) {
            Entity.Direction.DOWN -> currentFrame = when (currentState) {
                Entity.State.WALKING -> {
                    val animation = animations[AnimationType.WALK_DOWN] ?: return
                    animation.getKeyFrame(frameTime)
                }
                Entity.State.IDLE -> {
                    val animation = animations[AnimationType.WALK_DOWN] ?: return
                    animation.keyFrames[0]
                }
                Entity.State.IMMOBILE -> {
                    val animation = animations[AnimationType.IMMOBILE] ?: return
                    animation.getKeyFrame(frameTime)
                }
            }
            Entity.Direction.LEFT -> currentFrame = when (currentState) {
                Entity.State.WALKING -> {
                    val animation = animations[AnimationType.WALK_LEFT] ?: return
                    animation.getKeyFrame(frameTime)
                }
                Entity.State.IDLE -> {
                    val animation = animations[AnimationType.WALK_LEFT] ?: return
                    animation.keyFrames[0]
                }
                Entity.State.IMMOBILE -> {
                    val animation = animations[AnimationType.IMMOBILE] ?: return
                    animation.getKeyFrame(frameTime)
                }
            }
            Entity.Direction.UP -> currentFrame = when (currentState) {
                Entity.State.WALKING -> {
                    val animation = animations[AnimationType.WALK_UP] ?: return
                    animation.getKeyFrame(frameTime)
                }
                Entity.State.IDLE -> {
                    val animation = animations[AnimationType.WALK_UP] ?: return
                    animation.keyFrames[0]
                }
                Entity.State.IMMOBILE -> {
                    val animation = animations[AnimationType.IMMOBILE] ?: return
                    animation.getKeyFrame(frameTime)
                }
            }
            Entity.Direction.RIGHT -> currentFrame = when (currentState) {
                Entity.State.WALKING -> {
                    val animation = animations[AnimationType.WALK_RIGHT] ?: return
                    animation.getKeyFrame(frameTime)
                }
                Entity.State.IDLE -> {
                    val animation = animations[AnimationType.WALK_RIGHT] ?: return
                    animation.keyFrames[0]
                }
                Entity.State.IMMOBILE -> {
                    val animation = animations[AnimationType.IMMOBILE] ?: return
                    animation.getKeyFrame(frameTime)
                }
            }
        }
    }

    //Specific to two frame animations where each frame is stored in a separate texture
    protected fun loadAnimation(firstTexture: String, secondTexture: String, points: com.badlogic.gdx.utils.Array<GridPoint2>, frameDuration: Float): Animation<TextureRegion>? {
        Utility.loadTextureAsset(firstTexture)
        val texture1 = Utility.getTextureAsset(firstTexture)
        Utility.loadTextureAsset(secondTexture)
        val texture2 = Utility.getTextureAsset(secondTexture)
        val texture1Frames = TextureRegion.split(texture1, Entity.FRAME_WIDTH, Entity.FRAME_HEIGHT)
        val texture2Frames = TextureRegion.split(texture2, Entity.FRAME_WIDTH, Entity.FRAME_HEIGHT)
        val point = points.first()
        val animation: Animation<TextureRegion> = Animation(frameDuration, texture1Frames[point.x][point.y], texture2Frames[point.x][point.y])
        animation.playMode = Animation.PlayMode.LOOP
        return animation
    }

    protected fun loadAnimation(textureName: String, points: com.badlogic.gdx.utils.Array<GridPoint2>, frameDuration: Float): Animation<TextureRegion>? {
        Utility.loadTextureAsset(textureName)
        val texture = Utility.getTextureAsset(textureName)
        val textureFrames = TextureRegion.split(texture, Entity.FRAME_WIDTH, Entity.FRAME_HEIGHT)
        val animationKeyFrames = arrayOfNulls<TextureRegion>(points.size)
        points.forEachIndexed { index, point -> animationKeyFrames[index] = textureFrames[point.x][point.y] }

        val animation: Animation<TextureRegion> = Animation(frameDuration, *animationKeyFrames as Array<TextureRegion>)
        animation.playMode = Animation.PlayMode.LOOP
        return animation
    }

    fun getAnimation(type: AnimationType?): Animation<TextureRegion> {
        return animations[type]!!
    }

}