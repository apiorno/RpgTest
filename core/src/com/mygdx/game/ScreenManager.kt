package com.mygdx.game

import com.badlogic.gdx.Screen
import com.mygdx.game.windows.MainGameScreen
import com.mygdx.game.windows.MainMenuScreen

class ScreenManager(private val game: MyGdxGame) {
    enum class ScreenType {
        MainMenu, MainGame, LoadGame, NewGame, GameOver, WatchIntro, Credits
    }

    private var mainGameScreen: MainGameScreen? = null
    private var mainMenuScreen: MainMenuScreen? = null

    //private var loadGameScreen: LoadGameScreen? = null
    //private var newGameScreen: NewGameScreen? = null
    //private var gameOverScreen: GameOverScreen? = null
    //private var cutSceneScreen: CutSceneScreen? = null
    //private var creditScreen: CreditScreen? = null
    init {
        mainGameScreen = MainGameScreen(game)
        mainMenuScreen = MainMenuScreen(game)
        //loadGameScreen = LoadGameScreen(this)
        //newGameScreen = NewGameScreen(this)
        //gameOverScreen = GameOverScreen(this)
        //cutSceneScreen = CutSceneScreen(this)
        //creditScreen = CreditScreen(this)
    }

    fun getScreenType(screenType: ScreenType?): Screen? {
        return when (screenType) {
            ScreenType.MainMenu -> mainMenuScreen
            ScreenType.MainGame -> mainGameScreen
            /*ScreenType.LoadGame -> loadGameScreen
            ScreenType.NewGame -> newGameScreen
            ScreenType.GameOver -> gameOverScreen
            ScreenType.WatchIntro -> cutSceneScreen
            ScreenType.Credits -> creditScreen*/
            else -> mainMenuScreen
        }
    }
}