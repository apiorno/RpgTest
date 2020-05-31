package com.mygdx.game.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.MyGdxGame
import com.mygdx.game.ScreenManager.*
import com.mygdx.game.Utility
import com.mygdx.game.audio.AudioObserver
import com.mygdx.game.profile.ProfileManager
import ktx.collections.GdxArray
import ktx.scene2d.*

class LoadGameScreen (private val game: MyGdxGame) : GameScreen() {
    private val stage: Stage = Stage()
    lateinit var listItems: List<*>

    init {
        addActorsToStage()
    }
    private fun addActorsToStage(){

        val loadButton = TextButton("Load", Utility.STATUSUI_SKIN)
        val backButton = TextButton("Back", Utility.STATUSUI_SKIN)
        ProfileManager.instance?.storeAllProfiles()
        val list = ProfileManager.instance.profileList
        listItems = scene2d.listWidgetOf(list,"inventory",Utility.STATUSUI_SKIN)
        val scrollPane = ScrollPane (listItems)
        scrollPane.setOverscroll(false,false)
        scrollPane.fadeScrollBars = false
        scrollPane.setScrollingDisabled(true,false)
        scrollPane.setScrollbarsOnTop(true)

        stage.actors {
            //Layout
            table { center()
            setFillParent(true)
            padBottom(loadButton.height)
            add(scrollPane).center()}
            table { height=loadButton.height
            width = Gdx.graphics.width.toFloat()
            center()
            add(loadButton).padRight(50f)
            add(backButton)}
        }

        //Listeners
        backButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                game.setScreenOfType(ScreenType.MainMenu)
            }
        }
        )
        loadButton.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                if (listItems.selected == null) return
                val fileName = listItems.selected.toString()
                if (fileName != null && !fileName.isEmpty()) {
                    val file = ProfileManager.instance.getProfileFile(fileName)
                    if (file != null) {
                        ProfileManager.instance.setCurrentProfile(fileName)
                        notify(AudioObserver.AudioCommand.MUSIC_STOP, AudioObserver.AudioTypeEvent.MUSIC_TITLE)
                        game.setScreenOfType(ScreenType.MainGame)
                    }
                }
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
        val list = ProfileManager.instance.profileList
        listItems.setItems(list)
        Gdx.input.inputProcessor = stage
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