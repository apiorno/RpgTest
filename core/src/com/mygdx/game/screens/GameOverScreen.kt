package com.mygdx.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.mygdx.game.BludBourne
import com.mygdx.game.ScreenManager.*
import com.mygdx.game.Utility
import com.mygdx.game.audio.AudioObserver.AudioCommand
import com.mygdx.game.audio.AudioObserver.AudioTypeEvent

class GameOverScreen(private val _game: BludBourne) : GameScreen() {
    private val _stage: Stage = Stage()
    override fun render(delta: Float) {
        if (delta == 0f) {
            return
        }
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        _stage.act(delta)
        _stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        _stage.viewport.setScreenSize(width, height)
    }

    override fun show() {
        Gdx.input.inputProcessor = _stage
        notify(AudioCommand.MUSIC_PLAY_LOOP, AudioTypeEvent.MUSIC_TITLE)
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

    override fun pause() {}
    override fun resume() {}
    override fun dispose() {
        _stage.clear()
        _stage.dispose()
    }

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
        _stage.addActor(table)

        //Listeners
        continueButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                _game.changeScreenToType(ScreenType.LoadGame)
            }
        }
        )
        mainMenuButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                _game.changeScreenToType(ScreenType.MainMenu)
            }
        }
        )
        notify(AudioCommand.MUSIC_LOAD, AudioTypeEvent.MUSIC_TITLE)
    }
}