package com.mygdx.game

import com.mygdx.game.screens.*

class ScreenManager(private val game: BludBourne) {
    enum class ScreenType {
        MainMenu, MainGame, LoadGame, NewGame, GameOver, WatchIntro, Credits
    }
    private val mainGameScreen: MainGameScreen by lazy { MainGameScreen(game) }
    private val mainMenuScreen: MainMenuScreen by lazy { MainMenuScreen(game) }
    private val loadGameScreen: LoadGameScreen by lazy { LoadGameScreen(game) }
    private val newGameScreen: NewGameScreen by lazy { NewGameScreen(game) }
    private val gameOverScreen: GameOverScreen by lazy { GameOverScreen(game) }
    private val cutSceneScreen: CutSceneScreen by lazy { CutSceneScreen(game) }
    private val creditScreen: CreditScreen by lazy { CreditScreen(game) }

    init {
        game.apply {
            addScreen(mainGameScreen)
            addScreen(mainMenuScreen)
            addScreen(loadGameScreen)
            addScreen(newGameScreen)
            addScreen(gameOverScreen)
            addScreen(cutSceneScreen)
            addScreen(creditScreen)
        }
    }
    fun changeScreenToType(screenType: ScreenType) {
        return when (screenType) {
            ScreenType.MainMenu -> game.setScreen<MainMenuScreen>()
            ScreenType.MainGame -> game.setScreen<MainGameScreen>()
            ScreenType.LoadGame -> game.setScreen<LoadGameScreen>()
            ScreenType.NewGame -> game.setScreen<NewGameScreen>()
            ScreenType.GameOver -> game.setScreen<GameOverScreen>()
            ScreenType.WatchIntro -> game.setScreen<CutSceneScreen>()
            ScreenType.Credits -> game.setScreen<CreditScreen>()
        }
    }
     fun dispose() {
        mainGameScreen.dispose()
        mainMenuScreen.dispose()
        loadGameScreen.dispose()
        newGameScreen.dispose()
        gameOverScreen.dispose()
        creditScreen.dispose()
    }
}