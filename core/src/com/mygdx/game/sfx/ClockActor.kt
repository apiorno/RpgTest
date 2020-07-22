package com.mygdx.game.sfx

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin

class ClockActor : Label {
    enum class TimeOfDay {
        DAWN, AFTERNOON, DUSK, NIGHT
    }

    var totalTime = 0f
    var rateOfTime = 1f
    private var _isAfternoon = false

    constructor(text: CharSequence?, skin: Skin?) : super(text, skin) {
        init()
    }

    constructor(text: CharSequence?, skin: Skin?, styleName: String?) : super(text, skin, styleName) {
        init()
    }

    constructor(text: CharSequence?, skin: Skin?, fontName: String?, color: Color?) : super(text, skin, fontName, color) {
        init()
    }

    constructor(text: CharSequence?, skin: Skin?, fontName: String?, colorName: String?) : super(text, skin, fontName, colorName) {
        init()
    }

    constructor(text: CharSequence?, style: LabelStyle?) : super(text, style) {
        init()
    }

    private fun init() {
        val time = String.format(FORMAT, 0, 0, if (_isAfternoon) PM else AM)
        setText(time)
        pack()
    }

    val currentTimeOfDay: TimeOfDay
        get() {
            val hours = currentTimeHours
            return if (hours in 7..9) {
                TimeOfDay.DAWN
            } else if (hours in 10..16) {
                TimeOfDay.AFTERNOON
            } else if (hours in 17..19) {
                TimeOfDay.DUSK
            } else {
                TimeOfDay.NIGHT
            }
        }

    override fun act(delta: Float) {
        totalTime += delta * rateOfTime
        val seconds = currentTimeSeconds
        val minutes = currentTimeMinutes
        var hours = currentTimeHours
        _isAfternoon = !(hours == 24 || hours / 12 == 0)
        hours %= 12
        if (hours == 0) {
            hours = 12
        }
        val time = String.format(FORMAT, hours, minutes, if (_isAfternoon) PM else AM)
        setText(time)
    }

    private val currentTimeSeconds: Int
        get() = MathUtils.floor(totalTime % 60)

    private val currentTimeMinutes: Int
        get() = MathUtils.floor(totalTime / 60 % 60)

    private val currentTimeHours: Int
        get() {
            var hours = MathUtils.floor(totalTime / 3600 % 24)
            if (hours == 0) {
                hours = 24
            }
            return hours
        }

    companion object {
        private const val PM = "PM"
        private const val AM = "AM"
        private const val FORMAT = "%02d:%02d %s"
    }
}