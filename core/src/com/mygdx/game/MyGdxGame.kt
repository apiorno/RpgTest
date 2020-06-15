package com.mygdx.game

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.google.inject.*
import com.mygdx.game.systems.InputHandlerSystem

class MyGdxGame : Game() {
    lateinit var batch: SpriteBatch
    lateinit var engine : Engine
    private lateinit var screnManager : ScreenManager
    lateinit var injector : Injector
    lateinit var inputProcessor: PlayerInputProcessor

    @Override
    override fun create() {
        batch = SpriteBatch()
        injector = Guice.createInjector(GameModule(this))
        engine = injector.getInstance(Engine::class.java)
        injector.getInstance(Systems::class.java).list.map { injector.getInstance(it) }.forEach{ system -> engine.addSystem(system)}
        inputProcessor = injector.getInstance(PlayerInputProcessor::class.java)
        injector.getInstance(Systems::class.java).list.map { injector.getInstance(it) }.forEach{system -> engine.addSystem(system)}
        inputProcessor.add(engine.getSystem(InputHandlerSystem::class.java))
        Gdx.input.inputProcessor = inputProcessor
        screnManager = ScreenManager(this)
        setScreenOfType(ScreenManager.ScreenType.MainMenu)
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


