package com.mygdx.game.ecs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.MathUtils
import com.mygdx.game.ecs.Component.MESSAGE

class NPCInputComponent internal constructor() : InputComponent() {
    private var frameTime = 0.0f

    override fun receiveMessage(message: String) {
        val string: Array<String> = message.split(Component.MESSAGE_TOKEN).toTypedArray()
        if (string.isEmpty()) return

        //Specifically for messages with 1 object payload
        if (string.size == 1) {
            if (string[0].equals(MESSAGE.COLLISION_WITH_MAP.toString(), ignoreCase = true)) {
                currentDirection = Entity.Direction.randomNext
            } else if (string[0].equals(MESSAGE.COLLISION_WITH_ENTITY.toString(), ignoreCase = true)) {
                currentState = Entity.State.IDLE
                //_currentDirection = _currentDirection.getOpposite();
            }
        }
        if (string.size == 2) {
            if (string[0].equals(MESSAGE.INIT_STATE.toString(), ignoreCase = true)) {
                currentState = json.fromJson(Entity.State::class.java, string[1])
            } else if (string[0].equals(MESSAGE.INIT_DIRECTION.toString(), ignoreCase = true)) {
                currentDirection = json.fromJson(Entity.Direction::class.java, string[1])
            }
        }
    }

    override fun dispose() {}
    override fun update(entity: Entity, delta: Float) {
        if (keys[Keys.QUIT]!!) {
            Gdx.app.exit()
        }

        //If IMMOBILE, don't update anything
        if (currentState == Entity.State.IMMOBILE) {
            entity.sendMessage(MESSAGE.CURRENT_STATE, json.toJson(Entity.State.IMMOBILE))
            return
        }
        frameTime += delta

        //Change direction after so many seconds
        if (frameTime > MathUtils.random(1, 5)) {
            currentState = Entity.State.randomNext
            currentDirection = Entity.Direction.randomNext
            frameTime = 0.0f
        }
        if (currentState == Entity.State.IDLE) {
            entity.sendMessage(MESSAGE.CURRENT_STATE, json.toJson(Entity.State.IDLE))
            return
        }
        when (currentDirection) {
            Entity.Direction.LEFT -> {
                entity.sendMessage(MESSAGE.CURRENT_STATE, json.toJson(Entity.State.WALKING))
                entity.sendMessage(MESSAGE.CURRENT_DIRECTION, json.toJson(Entity.Direction.LEFT))
            }
            Entity.Direction.RIGHT -> {
                entity.sendMessage(MESSAGE.CURRENT_STATE, json.toJson(Entity.State.WALKING))
                entity.sendMessage(MESSAGE.CURRENT_DIRECTION, json.toJson(Entity.Direction.RIGHT))
            }
            Entity.Direction.UP -> {
                entity.sendMessage(MESSAGE.CURRENT_STATE, json.toJson(Entity.State.WALKING))
                entity.sendMessage(MESSAGE.CURRENT_DIRECTION, json.toJson(Entity.Direction.UP))
            }
            Entity.Direction.DOWN -> {
                entity.sendMessage(MESSAGE.CURRENT_STATE, json.toJson(Entity.State.WALKING))
                entity.sendMessage(MESSAGE.CURRENT_DIRECTION, json.toJson(Entity.Direction.DOWN))
            }
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.Q) {
            keys[Keys.QUIT] = true
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
        currentDirection = Entity.Direction.randomNext
        currentState = Entity.State.WALKING
    }
}