package com.mygdx.game

import AnimationType
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.GridPoint2
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.Array
import com.google.inject.*
import com.mygdx.game.systems.InputHandlerSystem
import java.util.*

class MyGdxGame : Game() {
    lateinit var batch: SpriteBatch
    lateinit var engine : Engine
    private lateinit var screnManager : ScreenManager
    lateinit var injector : Injector
    lateinit var inputProcessor: PlayerInputProcessor

    @Override
    override fun create() {
        batch = SpriteBatch()
        screnManager = ScreenManager(this)
        setScreenOfType(ScreenManager.ScreenType.MainMenu)
        injector = Guice.createInjector(GameModule(this))
        engine = injector.getInstance(Engine::class.java)
        inputProcessor = injector.getInstance(PlayerInputProcessor::class.java)
        injector.getInstance(Systems::class.java).list.map { injector.getInstance(it) }.forEach{system -> engine.addSystem(system)}
        inputProcessor.add(engine.getSystem(InputHandlerSystem::class.java))
        Gdx.input.inputProcessor = inputProcessor
    }
    fun setScreenOfType(screenType : ScreenManager.ScreenType){
        setScreen(screnManager.getScreenType(screenType))
    }



    /*@Override
    override fun render() {
        Gdx.gl.glClearColor(1f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        super.render()
        engine.update(Gdx.graphics.deltaTime)
    }*/

    @Override
    override fun dispose() {
        batch.dispose()
        screen.dispose()

    }
}


