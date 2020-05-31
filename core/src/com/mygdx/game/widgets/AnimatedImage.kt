package com.mygdx.game.widgets

import AnimationType
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.mygdx.game.EntityConfig
import com.mygdx.game.temporal.AnimationManager

class AnimatedImage : Image() {
    private var frameTime = 0f
    protected lateinit var animationManager: AnimationManager
    private var currentAnimationType = AnimationType.IDLE
    fun setEntityConfig(entityConfig: EntityConfig) {
        animationManager = AnimationManager(entityConfig)
        //set default
        setCurrentAnimation(AnimationType.IDLE)
    }

    fun setCurrentAnimation(animationType: AnimationType) {
        val animation = animationManager.getAnimation(animationType)
        currentAnimationType = animationType
        drawable = TextureRegionDrawable(animation.getKeyFrame(0f))
        setScaling(Scaling.stretch)
        setAlign(Align.center)
        setSize(this.prefWidth, this.prefHeight)
    }

    override fun act(delta: Float) {
        val drawable = drawable
                ?:
                return
        frameTime = (frameTime + delta) % 5
        val region = animationManager.getAnimation(currentAnimationType).getKeyFrame(frameTime, true)!!
        (drawable as TextureRegionDrawable).region = region
        super.act(delta)
    }

    companion object {
        private val TAG = AnimatedImage::class.java.simpleName
    }
}