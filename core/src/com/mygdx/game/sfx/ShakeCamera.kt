package com.mygdx.game.sfx

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

class ShakeCamera(x: Float, y: Float,private var shakeRadius: Float) {
    var isCameraShaking = false
        private set
    private var origShakeRadius = 30.0f
    private var randomAngle = 0f
    private val offset: Vector2
    private val currentPosition: Vector2
    private val origPosition: Vector2 = Vector2(x, y)
    fun setOrigPosition(x: Float, y: Float) {
        origPosition[x] = y
    }

    fun startShaking() {
        isCameraShaking = true
    }

    private fun seedRandomAngle() {
        randomAngle = MathUtils.random(1, 360).toFloat()
    }

    private fun computeCameraOffset() {
        val sine = MathUtils.sinDeg(randomAngle)
        val cosine = MathUtils.cosDeg(randomAngle)

        //Gdx.app.debug(TAG, "Sine of " + randomAngle + " is: " + sine);
        //Gdx.app.debug(TAG, "Cosine of " + randomAngle + " is: " + cosine);
        offset.x = cosine * shakeRadius
        offset.y = sine * shakeRadius

        //Gdx.app.debug(TAG, "Offset is x:" + offset.x + " , y: " + offset.y );
    }

    private fun computeCurrentPosition() {
        currentPosition.x = origPosition.x + offset.x
        currentPosition.y = origPosition.y + offset.y

        //Gdx.app.debug(TAG, "Current position is x:" + currentPosition.x + " , y: " + currentPosition.y );
    }

    private fun diminishShake() {
        //Gdx.app.debug(TAG, "Current shakeRadius is: " + shakeRadius + " randomAngle is: " + randomAngle);
        if (shakeRadius < 2.0) {
            //Gdx.app.debug(TAG, "Done shaking");
            reset()
            return
        }
        isCameraShaking = true
        shakeRadius *= .9f
        //Gdx.app.debug(TAG, "New shakeRadius is: " + shakeRadius);
        randomAngle = MathUtils.random(1, 360).toFloat()
        //Gdx.app.debug(TAG, "New random angle: " + randomAngle);
    }

    fun reset() {
        shakeRadius = origShakeRadius
        isCameraShaking = false
        seedRandomAngle()
        currentPosition.x = origPosition.x
        currentPosition.y = origPosition.y
    }

    val newShakePosition: Vector2
        get() {
            computeCameraOffset()
            computeCurrentPosition()
            diminishShake()
            return currentPosition
        }

    companion object {
        private val TAG = ShakeCamera::class.java.simpleName
    }

    init {
        origShakeRadius = shakeRadius
        offset = Vector2()
        currentPosition = Vector2()
        reset()
    }
}