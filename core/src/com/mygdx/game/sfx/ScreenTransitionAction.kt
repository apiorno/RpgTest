package com.mygdx.game.sfx

import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.actions.Actions

class ScreenTransitionAction : Action {
    enum class ScreenTransitionType {
        FADE_IN, FADE_OUT, NONE
    }

    var transitionType = ScreenTransitionType.NONE
    var transitionDuration = 3f

    constructor() {}
    constructor(type: ScreenTransitionType, duration: Float) {
        transitionType = type
        transitionDuration = duration
    }

    override fun act(delta: Float): Boolean {
        val actor = getTarget() ?: return false
        when (transitionType) {
            ScreenTransitionType.FADE_IN -> {
                val fadeIn = Actions.sequence(
                        Actions.alpha(1f),
                        Actions.fadeOut(transitionDuration))
                actor.addAction(fadeIn)
            }
            ScreenTransitionType.FADE_OUT -> {
                val fadeOut = Actions.sequence(
                        Actions.alpha(0f),
                        Actions.fadeIn(transitionDuration))
                actor.addAction(fadeOut)
            }
            ScreenTransitionType.NONE -> {
            }
            else -> {
            }
        }
        return true
    }

    companion object {
        @JvmStatic
        fun transition(type: ScreenTransitionType, duration: Float): ScreenTransitionAction {
            val action = Actions.action(ScreenTransitionAction::class.java)
            action.transitionType = type
            action.transitionDuration = duration
            return action
        }
    }
}