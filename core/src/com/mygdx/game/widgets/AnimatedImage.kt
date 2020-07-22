package com.mygdx.game.widgets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.mygdx.game.ecs.Entity
import com.mygdx.game.ecs.Entity.AnimationType

class AnimatedImage : Image() {
    private var frameTime = 0f
    private var entity: Entity? = null
    private var currentAnimationType = AnimationType.IDLE
    fun setEntity(entity: Entity?) {
        this.entity = entity
        //set default
        setCurrentAnimation(AnimationType.IDLE)
    }

    fun setCurrentAnimation(animationType: AnimationType) {
        val animation = entity!!.getAnimation(animationType)
        if (animation == null) {
            Gdx.app.debug(TAG, "Animation type $animationType does not exist!")
            return
        }
        currentAnimationType = animationType
        drawable = TextureRegionDrawable(animation.getKeyFrame(0f))
        setScaling(Scaling.stretch)
        align = Align.center
        setSize(this.prefWidth, this.prefHeight)
    }

    override fun act(delta: Float) {
        val drawable = drawable
                ?: //Gdx.app.debug(TAG, "Drawable is NULL!");
                return
        frameTime = (frameTime + delta) % 5
        val region = entity!!.getAnimation(currentAnimationType)!!.getKeyFrame(frameTime, true)!!
        //Gdx.app.debug(TAG, "Keyframe number is " + _animation.getKeyFrameIndex(_frameTime));
        (drawable as TextureRegionDrawable).region = region
        super.act(delta)
    }

    companion object {
        private val TAG = AnimatedImage::class.java.simpleName
    }
}