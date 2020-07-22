package com.mygdx.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.mygdx.game.BludBourne
import com.mygdx.game.ScreenManager.*
import com.mygdx.game.Utility
import com.mygdx.game.audio.AudioObserver.AudioCommand
import com.mygdx.game.audio.AudioObserver.AudioTypeEvent
import ktx.scene2d.*

class MainMenuScreen(private val game: BludBourne) : GameScreen() {

    private val stage: Stage = Stage()

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.setScreenSize(width, height)
    }

    override fun show() {
        notify(AudioCommand.MUSIC_PLAY_LOOP, AudioTypeEvent.MUSIC_TITLE)
        Gdx.input.inputProcessor = stage
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

    override fun dispose() {
        stage.dispose()
    }

    init {
        addActorsToStage()
    }

    private fun addActorsToStage() {
        val title = Image(Utility.STATUSUI_TEXTUREATLAS.findRegion("bludbourne_title"))
        val newGameButton = TextButton("New Game", Utility.STATUSUI_SKIN)
        val loadGameButton = TextButton("Load Game", Utility.STATUSUI_SKIN)
        val watchIntroButton = TextButton("Watch Intro", Utility.STATUSUI_SKIN)
        val creditsButton = TextButton("Credits", Utility.STATUSUI_SKIN)
        val exitButton = TextButton("Exit", Utility.STATUSUI_SKIN)

        stage.actors {
            table {
                setFillParent(true)
                add(title).spaceBottom(75f).row()
                add(newGameButton).spaceBottom(10f).row()
                add(loadGameButton).spaceBottom(10f).row()
                add(watchIntroButton).spaceBottom(10f).row()
                add(creditsButton).spaceBottom(10f).row()
                add(exitButton).spaceBottom(10f).row()
            }
        }

        //Listeners
        newGameButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                game.changeScreenToType(ScreenType.NewGame)
            }
        }
        )
        loadGameButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                game.changeScreenToType(ScreenType.LoadGame)
            }
        }
        )
        exitButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                Gdx.app.exit()
            }
        }
        )
        watchIntroButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                notify(AudioCommand.MUSIC_STOP, AudioTypeEvent.MUSIC_TITLE)
                game.changeScreenToType(ScreenType.WatchIntro)
            }
        }
        )
        creditsButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                game.changeScreenToType(ScreenType.Credits)
            }
        }
        )
        notify(AudioCommand.MUSIC_LOAD, AudioTypeEvent.MUSIC_TITLE)
    }

}