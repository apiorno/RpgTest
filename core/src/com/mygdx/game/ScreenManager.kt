package com.mygdx.game

import com.badlogic.gdx.Screen
import com.mygdx.game.windows.*

class ScreenManager(game: MyGdxGame) {
    enum class ScreenType {
        MainMenu, MainGame, LoadGame, NewGame, GameOver, WatchIntro, Credits
    }

    private var mainGameScreen: MainGameScreen? = null
    private var mainMenuScreen: MainMenuScreen? = null
    private var loadGameScreen: LoadGameScreen? = null
    private var newGameScreen: NewGameScreen? = null
    private var gameOverScreen: GameOverScreen? = null
    private var cutSceneScreen: CutSceneScreen? = null
    private var creditScreen: CreditScreen? = null

    init {
        mainGameScreen = MainGameScreen(game)
        mainMenuScreen = MainMenuScreen(game)
        loadGameScreen = LoadGameScreen(game)
        newGameScreen = NewGameScreen(game)
        gameOverScreen = GameOverScreen(game)
        cutSceneScreen = CutSceneScreen(game)
        creditScreen = CreditScreen(game)
    }

    fun getScreenType(screenType: ScreenType?): Screen? {
        return when (screenType) {
            ScreenType.MainMenu -> mainMenuScreen
            ScreenType.MainGame -> mainGameScreen
            ScreenType.LoadGame -> loadGameScreen
            ScreenType.NewGame -> newGameScreen
            ScreenType.GameOver -> gameOverScreen
            ScreenType.WatchIntro -> cutSceneScreen
            ScreenType.Credits -> creditScreen
            else -> mainMenuScreen
        }
    }
}