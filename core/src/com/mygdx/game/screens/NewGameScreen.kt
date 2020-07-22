package com.mygdx.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.mygdx.game.BludBourne
import com.mygdx.game.ScreenManager.*
import com.mygdx.game.Utility
import com.mygdx.game.audio.AudioObserver.AudioCommand
import com.mygdx.game.audio.AudioObserver.AudioTypeEvent
import com.mygdx.game.profile.ProfileManager
import ktx.scene2d.*

class NewGameScreen(private val game: BludBourne) : GameScreen() {
    private val stage: Stage = Stage()
    private val profileText: TextField by lazy { TextField("", Utility.STATUSUI_SKIN, "inventory") }
    private val overwriteDialog: Dialog by lazy { Dialog("Overwrite?", Utility.STATUSUI_SKIN, "solidbackground") }

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
        overwriteDialog.hide()
        profileText.text = ""
        Gdx.input.inputProcessor = stage
    }

    override fun hide() {
        overwriteDialog.hide()
        profileText.text = ""
        Gdx.input.inputProcessor = null
    }

    override fun dispose() {
        stage.clear()
        stage.dispose()
    }

    fun addActorsToStage(){
        val cancelButton = TextButton("Cancel", Utility.STATUSUI_SKIN, "inventory")
        val overwriteButton = TextButton("Overwrite", Utility.STATUSUI_SKIN, "inventory")
        val startButton = TextButton("Start", Utility.STATUSUI_SKIN)
        val backButton = TextButton("Back", Utility.STATUSUI_SKIN)
        profileText.maxLength = 20


        overwriteDialog.setKeepWithinStage(true)
        overwriteDialog.isModal = true
        overwriteDialog.isMovable = false
        overwriteDialog.text(scene2d.label("Overwrite existing profile name?", defaultStyle, Utility.STATUSUI_SKIN))

        //Layout
        overwriteDialog.row()
        overwriteDialog.button(overwriteButton).bottom().left()
        overwriteDialog.button(cancelButton).bottom().right()

        stage.actors {
            table {
                setFillParent(true)
                add(label("Enter Profile Name: ", defaultStyle,Utility.STATUSUI_SKIN)).center()
                add(profileText).center()
            }
            table {
                height = startButton.height
                width = Gdx.graphics.width.toFloat()
                center()
                add(startButton).padRight(50f)
                add(backButton)
            }

        }
        //Listeners
        cancelButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                overwriteDialog.hide()
            }
        }
        )
        overwriteButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                val messageText = profileText.text
                ProfileManager.writeProfileToStorage(messageText, "", true)
                ProfileManager.setCurrentProfile(messageText)
                ProfileManager.isNewProfile = true
                overwriteDialog.hide()
                notify(AudioCommand.MUSIC_STOP, AudioTypeEvent.MUSIC_TITLE)
                game.changeScreenToType(ScreenType.MainGame)
            }
        }
        )
        startButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                val messageText = profileText.text
                //check to see if the current profile matches one that already exists
                val exists: Boolean = ProfileManager.doesProfileExist(messageText)
                if (exists) {
                    //Pop up dialog for Overwrite
                    overwriteDialog.show(stage)
                } else {
                    ProfileManager.writeProfileToStorage(messageText, "", false)
                    ProfileManager.setCurrentProfile(messageText)
                    ProfileManager.isNewProfile = true
                    notify(AudioCommand.MUSIC_STOP, AudioTypeEvent.MUSIC_TITLE)
                    game.changeScreenToType(ScreenType.MainGame)
                }
            }
        }
        )
        backButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                game.changeScreenToType(ScreenType.MainMenu)
            }
        }
        )
    }
    init {
        addActorsToStage()
    }
}