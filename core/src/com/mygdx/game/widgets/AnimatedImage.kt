package com.mygdx.game.widgets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.mygdx.game.ecs.Entity
import com.mygdx.game.ecs.Entity.AnimationType

class AnimatedImage : Image() {
    private var _frameTime = 0f
    protected var _entity: Entity? = null
    private var _currentAnimationType = AnimationType.IDLE
    fun setEntity(entity: Entity?) {
        _entity = entity
        //set default
        setCurrentAnimation(AnimationType.IDLE)
    }

    fun setCurrentAnimation(animationType: AnimationType) {
        val animation = _entity!!.getAnimation(animationType)
        if (animation == null) {
            Gdx.app.debug(TAG, "Animation type $animationType does not exist!")
            return
        }
        _currentAnimationType = animationType
        drawable = TextureRegionDrawable(animation.getKeyFrame(0f))
        setScaling(Scaling.stretch)
        setAlign(Align.center)
        setSize(this.prefWidth, this.prefHeight)
    }

    override fun act(delta: Float) {
        val drawable = drawable
                ?: //Gdx.app.debug(TAG, "Drawable is NULL!");
                return
        _frameTime = (_frameTime + delta) % 5
        val region = _entity!!.getAnimation(_currentAnimationType)!!.getKeyFrame(_frameTime, true)!!
        //Gdx.app.debug(TAG, "Keyframe number is " + _animation.getKeyFrameIndex(_frameTime));
        (drawable as TextureRegionDrawable).region = region
        super.act(delta)
    }

    companion object {
        private val TAG = AnimatedImage::class.java.simpleName
    }
}