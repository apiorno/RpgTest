package com.mygdx.game.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.mygdx.game.MyGdxGame
import com.mygdx.game.ScreenManager.*
import com.mygdx.game.Utility
import com.mygdx.game.audio.AudioObserver
import com.mygdx.game.profile.ProfileManager

class NewGameScreen(private val game: MyGdxGame) : GameScreen() {
    private val stage: Stage = Stage()
    private val profileText: TextField
    private val overwriteDialog: Dialog

    init {

        //create
        val profileName = Label("Enter Profile Name: ", Utility.STATUSUI_SKIN)
        profileText = TextField("", Utility.STATUSUI_SKIN, "inventory")
        profileText.maxLength = 20
        overwriteDialog = Dialog("Overwrite?", Utility.STATUSUI_SKIN, "solidbackground")
        val overwriteLabel = Label("Overwrite existing profile name?", Utility.STATUSUI_SKIN)
        val cancelButton = TextButton("Cancel", Utility.STATUSUI_SKIN, "inventory")
        val overwriteButton = TextButton("Overwrite", Utility.STATUSUI_SKIN, "inventory")
        overwriteDialog.setKeepWithinStage(true)
        overwriteDialog.isModal = true
        overwriteDialog.isMovable = false
        overwriteDialog.text(overwriteLabel)
        val startButton = TextButton("Start", Utility.STATUSUI_SKIN)
        val backButton = TextButton("Back", Utility.STATUSUI_SKIN)

        //Layout
        overwriteDialog.row()
        overwriteDialog.button(overwriteButton).bottom().left()
        overwriteDialog.button(cancelButton).bottom().right()
        val topTable = Table()
        topTable.setFillParent(true)
        topTable.add(profileName).center()
        topTable.add(profileText).center()
        val bottomTable = Table()
        bottomTable.height = startButton.height
        bottomTable.width = Gdx.graphics.width.toFloat()
        bottomTable.center()
        bottomTable.add(startButton).padRight(50f)
        bottomTable.add(backButton)
        stage.addActor(topTable)
        stage.addActor(bottomTable)

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
                ProfileManager.instance.writeProfileToStorage(messageText, "", true)
                ProfileManager.instance.setCurrentProfile(messageText)
                ProfileManager.instance.isNewProfile = true
                overwriteDialog.hide()
                notify(AudioObserver.AudioCommand.MUSIC_STOP, AudioObserver.AudioTypeEvent.MUSIC_TITLE)
                game.setScreenOfType(ScreenType.MainGame)
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
                val exists = ProfileManager.instance.doesProfileExist(messageText)
                if (exists) {
                    //Pop up dialog for Overwrite
                    overwriteDialog.show(stage)
                } else {
                    ProfileManager.instance.writeProfileToStorage(messageText, "", false)
                    ProfileManager.instance.setCurrentProfile(messageText)
                    ProfileManager.instance.isNewProfile = true
                    notify(AudioObserver.AudioCommand.MUSIC_STOP, AudioObserver.AudioTypeEvent.MUSIC_TITLE)
                    game.setScreenOfType(ScreenType.MainGame)
                }
            }
        }
        )
        backButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                game.setScreenOfType(ScreenType.MainGame)
            }
        }
        )
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
        overwriteDialog.hide()
        profileText.text = ""
        Gdx.input.inputProcessor = stage
    }

    override fun hide() {
        overwriteDialog.hide()
        profileText.text = ""
        Gdx.input.inputProcessor = null
    }

    override fun pause() {}
    override fun resume() {}
    override fun dispose() {
        stage.clear()
        stage.dispose()
    }
}