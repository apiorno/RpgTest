package com.mygdx.game.temporal

interface StatusObserver {
    enum class StatusEvent {
        UPDATED_GP, UPDATED_LEVEL, UPDATED_HP, UPDATED_MP, UPDATED_XP, LEVELED_UP
    }

    fun onNotify(value: Int, event: StatusEvent?)
}