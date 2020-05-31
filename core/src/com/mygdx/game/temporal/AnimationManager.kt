package com.mygdx.game.temporal

import AnimationType
import FRAME_HEIGHT
import FRAME_WIDTH
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.GridPoint2
import com.badlogic.gdx.utils.Json
import com.mygdx.game.EntityConfig
import com.mygdx.game.Utility
import java.util.*

class AnimationManager (private val entityConfig: EntityConfig){
    protected var json = Json()
    protected  var animations: Hashtable<AnimationType, Animation<TextureRegion>> = Hashtable()

    init {
        loadAnimations()
    }

    private fun loadAnimations() {
        val animationConfigs = entityConfig.animationConfig
        for (animationConfig in animationConfigs) {
            val textureNames = animationConfig.texturePaths
            val points = animationConfig.gridPoints
            val animationType = animationConfig.animationType
            val frameDuration = animationConfig.frameDuration
            lateinit var animation: Animation<TextureRegion>
            if (textureNames.size == 1) {
                animation = loadAnimation(textureNames[0], points, frameDuration) as Animation<TextureRegion>
            } else if (textureNames.size == 2) {
                animation = loadAnimation(textureNames[0], textureNames[1], points, frameDuration)
            }
            animations[animationType] = animation
        }


    }

    //Specific to two frame animations where each frame is stored in a separate texture
    fun loadAnimation(firstTexture: String, secondTexture: String, points: com.badlogic.gdx.utils.Array<GridPoint2>, frameDuration: Float): Animation<TextureRegion> {
        Utility.loadTextureAsset(firstTexture)
        val texture1 = Utility.getTextureAsset(firstTexture)
        Utility.loadTextureAsset(secondTexture)
        val texture2 = Utility.getTextureAsset(secondTexture)
        val texture1Frames = TextureRegion.split(texture1, FRAME_WIDTH, FRAME_HEIGHT)
        val texture2Frames = TextureRegion.split(texture2, FRAME_WIDTH, FRAME_HEIGHT)
        val point = points.first()
        val animation: Animation<TextureRegion> = Animation<TextureRegion>(frameDuration, texture1Frames[point.x][point.y], texture2Frames[point.x][point.y])
        animation.playMode = Animation.PlayMode.LOOP
        return animation
    }

    fun loadAnimation(textureName: String, points: com.badlogic.gdx.utils.Array<GridPoint2>, frameDuration: Float): Animation<*> {
        Utility.loadTextureAsset(textureName)
        val texture = Utility.getTextureAsset(textureName)
        val textureFrames = TextureRegion.split(texture, FRAME_WIDTH, FRAME_HEIGHT)
        val animationKeyFrames = arrayOfNulls<TextureRegion>(points.size)
        for (i in 0 until points.size) {
            animationKeyFrames[i] = textureFrames[points[i].x][points[i].y]
        }
        val animation: Animation<TextureRegion> = Animation(frameDuration, *animationKeyFrames as Array<TextureRegion>)
        animation.playMode = Animation.PlayMode.LOOP
        return animation
    }

    fun getAnimation(type: AnimationType?): Animation<TextureRegion> {
        return animations[type]!!
    }
}