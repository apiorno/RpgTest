package com.mygdx.game

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor

class PlayerInputProcessor() : InputProcessor, IObservable{

    protected enum class Key {
        LEFT, RIGHT, UP, DOWN, QUIT, PAUSE
    }
    override val observers: ArrayList<IObserver> = ArrayList()

    override fun keyUp(keycode: Int): Boolean {
        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
            released(Key.LEFT)
        }
        if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
            released(Key.RIGHT)
        }
        if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
            released(Key.UP)
        }
        if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
            released(Key.DOWN)
        }
        if (keycode == Input.Keys.Q) {
            released(Key.QUIT)
        }
        if (keycode == Input.Keys.P) {
            released(Key.PAUSE)
        }
        return true
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
            pressed(Key.LEFT)
        }
        if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
            pressed(Key.RIGHT)
        }
        if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
            pressed(Key.UP)
        }
        if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
            pressed(Key.DOWN)
        }
        if (keycode == Input.Keys.Q) {
            pressed(Key.QUIT)
        }
        if (keycode == Input.Keys.P) {
            pressed(Key.PAUSE)
        }
        return true
    }
    private fun pressed(key: Key){
        notifyObservers(Event(EventType.INPUT_PRESSED,key.toString()))
    }
    private fun released(key: Key){
        notifyObservers(Event(EventType.INPUT_RELEASED,key.toString()))
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }
    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }
    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }
}