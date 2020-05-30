package com.mygdx.game.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.mygdx.game.MyGdxGame
import com.mygdx.game.ScreenManager.*
import com.mygdx.game.Utility
import com.mygdx.game.audio.AudioObserver

class GameOverScreen (private val game: MyGdxGame) : GameScreen() {
    private val stage: Stage = Stage()

    companion object {
        private const val DEATH_MESSAGE = "You have fought bravely, but alas, you have fallen during your epic struggle."
        private const val GAMEOVER = "Game Over"
    }

    init {

        //create
        val continueButton = TextButton("Continue", Utility.STATUSUI_SKIN)
        val mainMenuButton = TextButton("Main Menu", Utility.STATUSUI_SKIN)
        val messageLabel = Label(DEATH_MESSAGE, Utility.STATUSUI_SKIN)
        messageLabel.setWrap(true)
        val gameOverLabel = Label(GAMEOVER, Utility.STATUSUI_SKIN)
        gameOverLabel.setAlignment(Align.center)
        val table = Table()

        //Layout
        table.setFillParent(true)
        table.add(messageLabel).pad(50f, 50f, 50f, 50f).expandX().fillX().row()
        table.add(gameOverLabel)
        table.row()
        table.add(continueButton).pad(50f, 50f, 10f, 50f)
        table.row()
        table.add(mainMenuButton).pad(10f, 50f, 50f, 50f)
        stage.addActor(table)

        //Listeners
        continueButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                game.setScreenOfType(ScreenType.LoadGame)
            }
        }
        )
        mainMenuButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                game.setScreenOfType(ScreenType.MainMenu)
            }
        }
        )
        notify(AudioObserver.AudioCommand.MUSIC_LOAD, AudioObserver.AudioTypeEvent.MUSIC_TITLE)
    }

    override fun render(delta: Float) {
        if (delta == 0f) {
            return
        }
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.setScreenSize(width, height)
    }

    override fun show() {
        Gdx.input.inputProcessor = stage
        notify(AudioObserver.AudioCommand.MUSIC_PLAY_LOOP, AudioObserver.AudioTypeEvent.MUSIC_TITLE)
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

    override fun pause() {}
    override fun resume() {}
    override fun dispose() {
        stage.clear()
        stage.dispose()
    }
}