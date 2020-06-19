package com.mygdx.game.ecs

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.utils.Json
import java.util.*

abstract class InputComponent internal constructor() : ComponentSubject(), Component, InputProcessor {
    protected var _currentDirection: Entity.Direction? = null
    protected var _currentState: Entity.State? = null
    protected var _json: Json

    enum class Keys {
        LEFT, RIGHT, UP, DOWN, QUIT, PAUSE
    }

    enum class Mouse {
        SELECT, DOACTION
    }

    companion object {
        @JvmStatic
        protected var keys: MutableMap<Keys, Boolean> = HashMap()
        @JvmStatic
        protected var mouseButtons: MutableMap<Mouse, Boolean> = HashMap()

        //initialize the hashmap for inputs
        init {
            keys[Keys.LEFT] = false
            keys[Keys.RIGHT] = false
            keys[Keys.UP] = false
            keys[Keys.DOWN] = false
            keys[Keys.QUIT] = false
            keys[Keys.PAUSE] = false
        }

        init {
            mouseButtons[Mouse.SELECT] = false
            mouseButtons[Mouse.DOACTION] = false
        }
    }

    abstract fun update(entity: Entity, delta: Float)

    init {
        _json = Json()
    }
}