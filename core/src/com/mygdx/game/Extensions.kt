package com.mygdx.game

import com.badlogic.gdx.math.MathUtils

private val FACTOR_METER_TO_PIXELS = 16F

val Int.pixelToMeters : Float
    get() = this/ FACTOR_METER_TO_PIXELS

val Float.toDegrees : Float
    get() = MathUtils.radiansToDegrees * this