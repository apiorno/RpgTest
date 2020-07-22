package com.mygdx.game.ecs

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.utils.Json
import java.util.*

abstract class InputComponent internal constructor() : ComponentSubject(), Component, InputProcessor {
    protected var currentDirection: Entity.Direction? = null
    protected var currentState: Entity.State? = null
    protected var json: Json = Json()

    enum class Keys {
        LEFT, RIGHT, UP, DOWN, QUIT, PAUSE
    }

    enum class Mouse {
        SELECT, DOACTION
    }

    companion object {
        @JvmStatic
        protected var keys: MutableMap<Keys, Boolean> = EnumMap(Keys::class.java)
        @JvmStatic
        protected var mouseButtons: MutableMap<Mouse, Boolean> = EnumMap(Mouse::class.java)

        //initialize the hashmap for inputs
        init {
            keys[Keys.LEFT] = false
            keys[Keys.RIGHT] = false
            keys[Keys.UP] = false
            keys[Keys.DOWN] = false
            keys[Keys.QUIT] = false
            keys[Keys.PAUSE] = false
            mouseButtons[Mouse.SELECT] = false
            mouseButtons[Mouse.DOACTION] = false
        }
    }

    abstract fun update(entity: Entity, delta: Float)

}