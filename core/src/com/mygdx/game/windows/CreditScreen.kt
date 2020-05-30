package com.mygdx.game.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.mygdx.game.MyGdxGame
import com.mygdx.game.ScreenManager.*
import com.mygdx.game.Utility

class CreditScreen (private val game: MyGdxGame) : GameScreen() {
    private val stage: Stage = Stage()
    private var scrollPane: ScrollPane?

    companion object {
        private const val CREDITS_PATH = "licenses/credits.txt"
    }

    init {
        Gdx.input.inputProcessor = stage

        //Get text
        val file = Gdx.files.internal(CREDITS_PATH)
        val textString = file.readString()
        val text = Label(textString, Utility.STATUSUI_SKIN, "credits")
        text.setAlignment(Align.top or Align.center)
        text.setWrap(true)
        scrollPane = ScrollPane(text)
        scrollPane!!.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }

            override fun touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) {
                scrollPane!!.scrollY = 0f
                scrollPane!!.updateVisualScroll()
                game.setScreenOfType(ScreenType.MainMenu)
            }
        }
        )
        val table = Table()
        table.center()
        table.setFillParent(true)
        table.defaults().width(Gdx.graphics.width.toFloat())
        table.add(scrollPane)
        stage.addActor(table)
    }

    override fun render(delta: Float) {
        if (delta == 0f) {
            return
        }
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
        scrollPane!!.scrollY = scrollPane!!.scrollY + delta * 20
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.setScreenSize(width, height)
    }

    override fun show() {
        scrollPane!!.isVisible = true
        Gdx.input.inputProcessor = stage
    }

    override fun hide() {
        scrollPane!!.isVisible = false
        scrollPane!!.scrollY = 0f
        scrollPane!!.updateVisualScroll()
        Gdx.input.inputProcessor = null
    }

    override fun pause() {}
    override fun resume() {}
    override fun dispose() {
        stage.clear()
        scrollPane = null
        stage.dispose()
    }
}