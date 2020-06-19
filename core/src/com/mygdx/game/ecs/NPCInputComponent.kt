package com.mygdx.game.ecs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.MathUtils
import com.mygdx.game.ecs.Component.MESSAGE

class NPCInputComponent internal constructor() : InputComponent() {
    private var _frameTime = 0.0f
    override fun receiveMessage(message: String) {
        val string: Array<String> = message.split(Component.Companion.MESSAGE_TOKEN).toTypedArray()
        if (string.size == 0) return

        //Specifically for messages with 1 object payload
        if (string.size == 1) {
            if (string[0].equals(MESSAGE.COLLISION_WITH_MAP.toString(), ignoreCase = true)) {
                _currentDirection = Entity.Direction.Companion.randomNext
            } else if (string[0].equals(MESSAGE.COLLISION_WITH_ENTITY.toString(), ignoreCase = true)) {
                _currentState = Entity.State.IDLE
                //_currentDirection = _currentDirection.getOpposite();
            }
        }
        if (string.size == 2) {
            if (string[0].equals(MESSAGE.INIT_STATE.toString(), ignoreCase = true)) {
                _currentState = _json.fromJson(Entity.State::class.java, string[1])
            } else if (string[0].equals(MESSAGE.INIT_DIRECTION.toString(), ignoreCase = true)) {
                _currentDirection = _json.fromJson(Entity.Direction::class.java, string[1])
            }
        }
    }

    override fun dispose() {}
    override fun update(entity: Entity, delta: Float) {
        if (keys[Keys.QUIT]!!) {
            Gdx.app.exit()
        }

        //If IMMOBILE, don't update anything
        if (_currentState == Entity.State.IMMOBILE) {
            entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(Entity.State.IMMOBILE))
            return
        }
        _frameTime += delta

        //Change direction after so many seconds
        if (_frameTime > MathUtils.random(1, 5)) {
            _currentState = Entity.State.Companion.randomNext
            _currentDirection = Entity.Direction.Companion.randomNext
            _frameTime = 0.0f
        }
        if (_currentState == Entity.State.IDLE) {
            entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(Entity.State.IDLE))
            return
        }
        when (_currentDirection) {
            Entity.Direction.LEFT -> {
                entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(Entity.State.WALKING))
                entity.sendMessage(MESSAGE.CURRENT_DIRECTION, _json.toJson(Entity.Direction.LEFT))
            }
            Entity.Direction.RIGHT -> {
                entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(Entity.State.WALKING))
                entity.sendMessage(MESSAGE.CURRENT_DIRECTION, _json.toJson(Entity.Direction.RIGHT))
            }
            Entity.Direction.UP -> {
                entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(Entity.State.WALKING))
                entity.sendMessage(MESSAGE.CURRENT_DIRECTION, _json.toJson(Entity.Direction.UP))
            }
            Entity.Direction.DOWN -> {
                entity.sendMessage(MESSAGE.CURRENT_STATE, _json.toJson(Entity.State.WALKING))
                entity.sendMessage(MESSAGE.CURRENT_DIRECTION, _json.toJson(Entity.Direction.DOWN))
            }
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.Q) {
            InputComponent.Companion.keys.put(Keys.QUIT, true)
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    companion object {
        private val TAG = NPCInputComponent::class.java.simpleName
    }

    init {
        _currentDirection = Entity.Direction.Companion.randomNext
        _currentState = Entity.State.WALKING
    }
}