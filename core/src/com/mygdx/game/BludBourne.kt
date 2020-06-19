package com.mygdx.game

import com.badlogic.gdx.Game
import com.badlogic.gdx.Screen
import com.mygdx.game.screens.*

class BludBourne : Game() {
    enum class ScreenType {
        MainMenu, MainGame, LoadGame, NewGame, GameOver, WatchIntro, Credits
    }

    fun getScreenType(screenType: ScreenType?): Screen? {
        return when (screenType) {
            ScreenType.MainMenu -> _mainMenuScreen
            ScreenType.MainGame -> _mainGameScreen
            ScreenType.LoadGame -> _loadGameScreen
            ScreenType.NewGame -> _newGameScreen
            ScreenType.GameOver -> _gameOverScreen
            ScreenType.WatchIntro -> _cutSceneScreen
            ScreenType.Credits -> _creditScreen
            else -> _mainMenuScreen
        }
    }

    override fun create() {
        _mainGameScreen = MainGameScreen(this)
        _mainMenuScreen = MainMenuScreen(this)
        _loadGameScreen = LoadGameScreen(this)
        _newGameScreen = NewGameScreen(this)
        _gameOverScreen = GameOverScreen(this)
        _cutSceneScreen = CutSceneScreen(this)
        _creditScreen = CreditScreen(this)
        setScreen(_mainMenuScreen)
    }

    override fun dispose() {
        _mainGameScreen!!.dispose()
        _mainMenuScreen!!.dispose()
        _loadGameScreen!!.dispose()
        _newGameScreen!!.dispose()
        _gameOverScreen!!.dispose()
        _creditScreen!!.dispose()
    }

    companion object {
        private var _mainGameScreen: MainGameScreen? = null
        private var _mainMenuScreen: MainMenuScreen? = null
        private var _loadGameScreen: LoadGameScreen? = null
        private var _newGameScreen: NewGameScreen? = null
        private var _gameOverScreen: GameOverScreen? = null
        private var _cutSceneScreen: CutSceneScreen? = null
        private var _creditScreen: CreditScreen? = null
    }
}