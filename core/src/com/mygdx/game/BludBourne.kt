package com.mygdx.game

import com.mygdx.game.ScreenManager.*
import com.mygdx.game.screens.GameScreen
import ktx.app.KtxGame


class BludBourne : KtxGame<GameScreen>() {
    private val screenManager:ScreenManager by lazy { ScreenManager(this)}

    fun changeScreenToType(screenType: ScreenType) {
        screenManager.changeScreenToType(screenType)
    }

    override fun create() {
        changeScreenToType(ScreenType.MainMenu)
    }

    override fun dispose() {
        screenManager.dispose()
    }

}