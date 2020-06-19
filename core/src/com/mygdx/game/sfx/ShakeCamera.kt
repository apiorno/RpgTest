package com.mygdx.game.sfx

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

class ShakeCamera(x: Float, y: Float, shakeRadius: Float) {
    var isCameraShaking = false
        private set
    private var _origShakeRadius = 30.0f
    private var _shakeRadius: Float
    private var _randomAngle = 0f
    private val _offset: Vector2
    private val _currentPosition: Vector2
    private val _origPosition: Vector2
    fun setOrigPosition(x: Float, y: Float) {
        _origPosition[x] = y
    }

    fun startShaking() {
        isCameraShaking = true
    }

    private fun seedRandomAngle() {
        _randomAngle = MathUtils.random(1, 360).toFloat()
    }

    private fun computeCameraOffset() {
        val sine = MathUtils.sinDeg(_randomAngle)
        val cosine = MathUtils.cosDeg(_randomAngle)

        //Gdx.app.debug(TAG, "Sine of " + _randomAngle + " is: " + sine);
        //Gdx.app.debug(TAG, "Cosine of " + _randomAngle + " is: " + cosine);
        _offset.x = cosine * _shakeRadius
        _offset.y = sine * _shakeRadius

        //Gdx.app.debug(TAG, "Offset is x:" + _offset.x + " , y: " + _offset.y );
    }

    private fun computeCurrentPosition() {
        _currentPosition.x = _origPosition.x + _offset.x
        _currentPosition.y = _origPosition.y + _offset.y

        //Gdx.app.debug(TAG, "Current position is x:" + _currentPosition.x + " , y: " + _currentPosition.y );
    }

    private fun diminishShake() {
        //Gdx.app.debug(TAG, "Current shakeRadius is: " + _shakeRadius + " randomAngle is: " + _randomAngle);
        if (_shakeRadius < 2.0) {
            //Gdx.app.debug(TAG, "Done shaking");
            reset()
            return
        }
        isCameraShaking = true
        _shakeRadius *= .9f
        //Gdx.app.debug(TAG, "New shakeRadius is: " + _shakeRadius);
        _randomAngle = MathUtils.random(1, 360).toFloat()
        //Gdx.app.debug(TAG, "New random angle: " + _randomAngle);
    }

    fun reset() {
        _shakeRadius = _origShakeRadius
        isCameraShaking = false
        seedRandomAngle()
        _currentPosition.x = _origPosition.x
        _currentPosition.y = _origPosition.y
    }

    val newShakePosition: Vector2
        get() {
            computeCameraOffset()
            computeCurrentPosition()
            diminishShake()
            return _currentPosition
        }

    companion object {
        private val TAG = ShakeCamera::class.java.simpleName
    }

    init {
        _origPosition = Vector2(x, y)
        _shakeRadius = shakeRadius
        _origShakeRadius = shakeRadius
        _offset = Vector2()
        _currentPosition = Vector2()
        reset()
    }
}