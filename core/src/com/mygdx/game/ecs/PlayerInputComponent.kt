package com.mygdx.game.ecs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector3
import com.mygdx.game.ecs.Component.MESSAGE
import com.mygdx.game.screens.MainGameScreen

class PlayerInputComponent : InputComponent() {

    private val lastMouseCoordinates: Vector3 = Vector3()

    override fun receiveMessage(message: String) {
        val string: Array<String> = message.split(Component.MESSAGE_TOKEN).toTypedArray()
        if (string.isEmpty()) return

        //Specifically for messages with 1 object payload
        if (string.size == 2) {
            if (string[0].equals(MESSAGE.CURRENT_DIRECTION.toString(), ignoreCase = true)) {
                currentDirection = json.fromJson(Entity.Direction::class.java, string[1])
            }
        }
    }

    override fun dispose() {
        Gdx.input.inputProcessor = null
    }

    override fun update(entity: Entity, delta: Float) {
        //Keyboard input
        if (keys[Keys.PAUSE]!!) {
            MainGameScreen.setGameState(MainGameScreen.GameState.PAUSED)
            pauseReleased()
        } else if (keys[Keys.LEFT]!!) {
            entity.sendMessage(MESSAGE.CURRENT_STATE, json.toJson(Entity.State.WALKING))
            entity.sendMessage(MESSAGE.CURRENT_DIRECTION, json.toJson(Entity.Direction.LEFT))
        } else if (keys[Keys.RIGHT]!!) {
            entity.sendMessage(MESSAGE.CURRENT_STATE, json.toJson(Entity.State.WALKING))
            entity.sendMessage(MESSAGE.CURRENT_DIRECTION, json.toJson(Entity.Direction.RIGHT))
        } else if (keys[Keys.UP]!!) {
            entity.sendMessage(MESSAGE.CURRENT_STATE, json.toJson(Entity.State.WALKING))
            entity.sendMessage(MESSAGE.CURRENT_DIRECTION, json.toJson(Entity.Direction.UP))
        } else if (keys[Keys.DOWN]!!) {
            entity.sendMessage(MESSAGE.CURRENT_STATE, json.toJson(Entity.State.WALKING))
            entity.sendMessage(MESSAGE.CURRENT_DIRECTION, json.toJson(Entity.Direction.DOWN))
        } else if (keys[Keys.QUIT]!!) {
            quitReleased()
            Gdx.app.exit()
        } else {
            entity.sendMessage(MESSAGE.CURRENT_STATE, json.toJson(Entity.State.IDLE))
            if (currentDirection == null) {
                entity.sendMessage(MESSAGE.CURRENT_DIRECTION, json.toJson(Entity.Direction.DOWN))
            }
        }

        //Mouse input
        if (mouseButtons[Mouse.SELECT]!!) {
            //Gdx.app.debug(TAG, "Mouse LEFT click at : (" + _lastMouseCoordinates.x + "," + _lastMouseCoordinates.y + ")" );
            entity.sendMessage(MESSAGE.INIT_SELECT_ENTITY, json.toJson(lastMouseCoordinates))
            mouseButtons[Mouse.SELECT] = false
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
            leftPressed()
        }
        if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
            rightPressed()
        }
        if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
            upPressed()
        }
        if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
            downPressed()
        }
        if (keycode == Input.Keys.Q) {
            quitPressed()
        }
        if (keycode == Input.Keys.P) {
            pausePressed()
        }
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
            leftReleased()
        }
        if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
            rightReleased()
        }
        if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
            upReleased()
        }
        if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
            downReleased()
        }
        if (keycode == Input.Keys.Q) {
            quitReleased()
        }
        if (keycode == Input.Keys.P) {
            pauseReleased()
        }
        return true
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        //Gdx.app.debug(TAG, "GameScreen: MOUSE DOWN........: (" + screenX + "," + screenY + ")" );
        if (button == Input.Buttons.LEFT || button == Input.Buttons.RIGHT) {
            setClickedMouseCoordinates(screenX, screenY)
        }

        //left is selection, right is context menu
        if (button == Input.Buttons.LEFT) {
            selectMouseButtonPressed(screenX, screenY)
        }
        if (button == Input.Buttons.RIGHT) {
            doActionMouseButtonPressed(screenX, screenY)
        }
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        //left is selection, right is context menu
        if (button == Input.Buttons.LEFT) {
            selectMouseButtonReleased(screenX, screenY)
        }
        if (button == Input.Buttons.RIGHT) {
            doActionMouseButtonReleased(screenX, screenY)
        }
        return true
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

    //Key presses
    private fun leftPressed() {
        keys[Keys.LEFT] = true
    }

    private fun rightPressed() {
        keys[Keys.RIGHT] = true
    }

    private fun upPressed() {
        keys[Keys.UP] = true
    }

    private fun downPressed() {
        keys[Keys.DOWN] = true
    }

    private fun quitPressed() {
        keys[Keys.QUIT] = true
    }

    private fun pausePressed() {
        keys[Keys.PAUSE] = true
    }

    private fun setClickedMouseCoordinates(x: Int, y: Int) {
        lastMouseCoordinates[x.toFloat(), y.toFloat()] = 0f
    }

    private fun selectMouseButtonPressed(x: Int, y: Int) {
        mouseButtons[Mouse.SELECT] = true
    }

    private fun doActionMouseButtonPressed(x: Int, y: Int) {
        mouseButtons[Mouse.DOACTION] = true
    }

    //Releases
    private fun leftReleased() {
        keys[Keys.LEFT] = false
    }

    private fun rightReleased() {
        keys[Keys.RIGHT] = false
    }

    private fun upReleased() {
        keys[Keys.UP] = false
    }

    private fun downReleased() {
        keys[Keys.DOWN] = false
    }

    private fun quitReleased() {
        keys[Keys.QUIT] = false
    }

    private fun pauseReleased() {
        keys[Keys.PAUSE] = false
    }

    private fun selectMouseButtonReleased(x: Int, y: Int) {
        mouseButtons[Mouse.SELECT] = false
    }

    private fun doActionMouseButtonReleased(x: Int, y: Int) {
        mouseButtons[Mouse.DOACTION] = false
    }

    companion object {
        private val TAG = PlayerInputComponent::class.java.simpleName
        fun clear() {
            keys[Keys.LEFT] = false
            keys[Keys.RIGHT] = false
            keys[Keys.UP] = false
            keys[Keys.DOWN] = false
            keys[Keys.QUIT] = false
        }
    }

}