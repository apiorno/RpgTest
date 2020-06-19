package com.mygdx.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.mygdx.game.BludBourne
import com.mygdx.game.BludBourne.ScreenType
import com.mygdx.game.Utility
import com.mygdx.game.audio.AudioObserver.AudioCommand
import com.mygdx.game.audio.AudioObserver.AudioTypeEvent
import com.mygdx.game.profile.ProfileManager

class NewGameScreen(private val _game: BludBourne) : GameScreen() {
    private val _stage: Stage
    private val _profileText: TextField
    private val _overwriteDialog: Dialog
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
        _overwriteDialog.hide()
        _profileText.text = ""
        Gdx.input.inputProcessor = _stage
    }

    override fun hide() {
        _overwriteDialog.hide()
        _profileText.text = ""
        Gdx.input.inputProcessor = null
    }

    override fun pause() {}
    override fun resume() {}
    override fun dispose() {
        _stage.clear()
        _stage.dispose()
    }

    init {

        //create
        _stage = Stage()
        val profileName = Label("Enter Profile Name: ", Utility.STATUSUI_SKIN)
        _profileText = TextField("", Utility.STATUSUI_SKIN, "inventory")
        _profileText.maxLength = 20
        _overwriteDialog = Dialog("Overwrite?", Utility.STATUSUI_SKIN, "solidbackground")
        val overwriteLabel = Label("Overwrite existing profile name?", Utility.STATUSUI_SKIN)
        val cancelButton = TextButton("Cancel", Utility.STATUSUI_SKIN, "inventory")
        val overwriteButton = TextButton("Overwrite", Utility.STATUSUI_SKIN, "inventory")
        _overwriteDialog.setKeepWithinStage(true)
        _overwriteDialog.isModal = true
        _overwriteDialog.isMovable = false
        _overwriteDialog.text(overwriteLabel)
        val startButton = TextButton("Start", Utility.STATUSUI_SKIN)
        val backButton = TextButton("Back", Utility.STATUSUI_SKIN)

        //Layout
        _overwriteDialog.row()
        _overwriteDialog.button(overwriteButton).bottom().left()
        _overwriteDialog.button(cancelButton).bottom().right()
        val topTable = Table()
        topTable.setFillParent(true)
        topTable.add(profileName).center()
        topTable.add(_profileText).center()
        val bottomTable = Table()
        bottomTable.height = startButton.height
        bottomTable.width = Gdx.graphics.width.toFloat()
        bottomTable.center()
        bottomTable.add(startButton).padRight(50f)
        bottomTable.add(backButton)
        _stage.addActor(topTable)
        _stage.addActor(bottomTable)

        //Listeners
        cancelButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                _overwriteDialog.hide()
            }
        }
        )
        overwriteButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                val messageText = _profileText.text
                ProfileManager.instance.writeProfileToStorage(messageText, "", true)
                ProfileManager.instance.setCurrentProfile(messageText)
                ProfileManager.instance.isNewProfile = true
                _overwriteDialog.hide()
                notify(AudioCommand.MUSIC_STOP, AudioTypeEvent.MUSIC_TITLE)
                _game.screen = _game.getScreenType(ScreenType.MainGame)
            }
        }
        )
        startButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                val messageText = _profileText.text
                //check to see if the current profile matches one that already exists
                var exists = false
                exists = ProfileManager.instance.doesProfileExist(messageText)
                if (exists) {
                    //Pop up dialog for Overwrite
                    _overwriteDialog.show(_stage)
                } else {
                    ProfileManager.instance.writeProfileToStorage(messageText, "", false)
                    ProfileManager.instance.setCurrentProfile(messageText)
                    ProfileManager.instance.isNewProfile = true
                    notify(AudioCommand.MUSIC_STOP, AudioTypeEvent.MUSIC_TITLE)
                    _game.screen = _game.getScreenType(ScreenType.MainGame)
                }
            }
        }
        )
        backButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                _game.screen = _game.getScreenType(ScreenType.MainMenu)
            }
        }
        )
    }
}