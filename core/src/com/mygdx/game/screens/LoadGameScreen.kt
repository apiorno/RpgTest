package com.mygdx.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.mygdx.game.BludBourne
import com.mygdx.game.BludBourne.ScreenType
import com.mygdx.game.Utility
import com.mygdx.game.audio.AudioManager
import com.mygdx.game.audio.AudioObserver.AudioCommand
import com.mygdx.game.audio.AudioObserver.AudioTypeEvent
import com.mygdx.game.profile.ProfileManager
import ktx.scene2d.actors
import ktx.scene2d.listWidgetOf
import ktx.scene2d.scene2d
import ktx.scene2d.table

class LoadGameScreen(private val _game: BludBourne) : GameScreen() {
    private val _stage: Stage = Stage()
    private lateinit var _listItems: List<*>
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
        val list = ProfileManager.instance.profileList
        _listItems.setItems(list)
        Gdx.input.inputProcessor = _stage
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

    init {
        addActorsToStage()
    }

    private fun addActorsToStage() {

        val loadButton = TextButton("Load", Utility.STATUSUI_SKIN)
        val backButton = TextButton("Back", Utility.STATUSUI_SKIN)
        ProfileManager.instance.storeAllProfiles()
        val list = ProfileManager.instance.profileList
        _listItems = scene2d.listWidgetOf(list, "inventory", Utility.STATUSUI_SKIN)
        val scrollPane = ScrollPane(_listItems)
        scrollPane.setOverscroll(false, false)
        scrollPane.fadeScrollBars = false
        scrollPane.setScrollingDisabled(true, false)
        scrollPane.setScrollbarsOnTop(true)

        _stage.actors {
            //Layout
            table {
                center()
                setFillParent(true)
                padBottom(loadButton.height)
                add(scrollPane).center()
            }
            table {
                height = loadButton.height
                width = Gdx.graphics.width.toFloat()
                center()
                add(loadButton).padRight(50f)
                add(backButton)
            }
        }

        //Listeners
        backButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                _game.screen = _game.getScreenType(ScreenType.MainMenu)
            }
        }
        )
        loadButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (_listItems.selected == null) return
                val fileName = _listItems.selected.toString()
                if (fileName != null && !fileName.isEmpty()) {
                    val file = ProfileManager.instance.getProfileFile(fileName)
                    if (file != null) {
                        ProfileManager.instance.setCurrentProfile(fileName)
                        notify(AudioCommand.MUSIC_STOP, AudioTypeEvent.MUSIC_TITLE)
                        _game.screen = _game.getScreenType(ScreenType.MainGame)
                    }
                }
            }
        }
        )
    }
}