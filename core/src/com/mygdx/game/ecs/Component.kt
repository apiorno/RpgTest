package com.mygdx.game.ecs

interface Component {
    enum class MESSAGE {
        CURRENT_POSITION, INIT_START_POSITION, CURRENT_DIRECTION, CURRENT_STATE, COLLISION_WITH_MAP, COLLISION_WITH_ENTITY, LOAD_ANIMATIONS, INIT_DIRECTION, INIT_STATE, INIT_SELECT_ENTITY, ENTITY_SELECTED, ENTITY_DESELECTED
    }

    fun dispose()
    fun receiveMessage(message: String)

    companion object {
        const val MESSAGE_TOKEN = ":::::"
    }
}