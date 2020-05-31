package com.mygdx.game.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.utils.Json
import com.mygdx.game.MyGdxGame
import com.mygdx.game.PlayerInputProcessor
import com.mygdx.game.ScreenManager
import com.mygdx.game.profile.ProfileManager
import com.mygdx.game.temporal.EntityFactory
import com.mygdx.game.temporal.EntityFactory.Companion.getEntity

open class MainGameScreen  (private val game: MyGdxGame) :Screen {
    object VIEWPORT {
        var viewportWidth = 0f
        var viewportHeight = 0f
        var virtualWidth = 0f
        var virtualHeight = 0f
        var physicalWidth = 0f
        var physicalHeight = 0f
        var aspectRatio = 0f
    }

    enum class GameState {
        SAVING, LOADING, RUNNING, PAUSED, GAME_OVER
    }
    protected var mapMgr: MapManager

    protected var hudCamera: OrthographicCamera? = null
    private val json: Json
    private val multiplexer: InputMultiplexer
    private val playerHUD: PlayerHUD

    companion object {
        private val TAG = MainGameScreen::class.java.simpleName
        private var gameState: GameState? = null
        @JvmStatic
        fun setGameState(state: GameState?) {
            when (state) {
                GameState.RUNNING -> gameState = GameState.RUNNING
                GameState.LOADING -> {
                    ProfileManager.instance.loadProfile()
                    gameState = GameState.RUNNING
                }
                GameState.SAVING -> {
                    ProfileManager.instance.saveProfile()
                    gameState = GameState.PAUSED
                }
                GameState.PAUSED -> if (gameState == GameState.PAUSED) {
                    gameState = GameState.RUNNING
                } else if (gameState == GameState.RUNNING) {
                    gameState = GameState.PAUSED
                }
                GameState.GAME_OVER -> gameState = GameState.GAME_OVER
                else -> gameState = GameState.RUNNING
            }
        }
    }

    init {
        mapMgr = MapManager()
        json = Json()
        setGameState(GameState.RUNNING)

        //_camera setup
        setupViewport(10, 10)

        //get the current size
        camera = game.injector.getInstance(OrthographicCamera::class.java)
        camera!!.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight)
        player = getEntity(EntityType.PLAYER)
        mapMgr.player = player
        mapMgr.camera = camera
        hudCamera = OrthographicCamera()
        hudCamera!!.setToOrtho(false, VIEWPORT.physicalWidth, VIEWPORT.physicalHeight)
        playerHUD = PlayerHUD(hudCamera, player, mapMgr)
        multiplexer = InputMultiplexer()
        multiplexer.addProcessor(playerHUD.stage)
        multiplexer.addProcessor(game.inputProcessor)
        Gdx.input.inputProcessor = multiplexer
    }

    override fun hide() {
        TODO("Not yet implemented")
    }

    override fun show() {

    }

    override fun render(delta: Float) {
        if (gameState == GameState.GAME_OVER) {
            game.setScreenOfType(ScreenType.GameOver)
        }
        if (gameState == GameState.PAUSED) {
            player!!.updateInput(delta)
            playerHUD.render(delta)
            return
        }
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        mapRenderer!!.setView(camera)
        mapRenderer!!.batch.enableBlending()
        mapRenderer!!.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        if (mapMgr.hasMapChanged()) {
            mapRenderer!!.map = mapMgr.currentTiledMap
            player!!.sendMessage(MESSAGE.INIT_START_POSITION, json.toJson(mapMgr.playerStartUnitScaled))
            camera!!.position[mapMgr.playerStartUnitScaled!!.x, mapMgr.playerStartUnitScaled!!.y] = 0f
            camera!!.update()
            playerHUD.updateEntityObservers()
            mapMgr.setMapChanged(false)
            playerHUD.addTransitionToScreen()
        }
        mapMgr.updateLightMaps(playerHUD.currentTimeOfDay)
        val lightMap = mapMgr.currentLightMapLayer as TiledMapImageLayer?
        val previousLightMap = mapMgr.previousLightMapLayer as TiledMapImageLayer?
        if (lightMap != null) {
            mapRenderer!!.batch.begin()
            val backgroundMapLayer = mapMgr.currentTiledMap!!.layers[Map.BACKGROUND_LAYER] as TiledMapTileLayer
            if (backgroundMapLayer != null) {
                mapRenderer!!.renderTileLayer(backgroundMapLayer)
            }
            val groundMapLayer = mapMgr.currentTiledMap!!.layers[Map.GROUND_LAYER] as TiledMapTileLayer
            if (groundMapLayer != null) {
                mapRenderer!!.renderTileLayer(groundMapLayer)
            }
            val decorationMapLayer = mapMgr.currentTiledMap!!.layers[Map.DECORATION_LAYER] as TiledMapTileLayer
            if (decorationMapLayer != null) {
                mapRenderer!!.renderTileLayer(decorationMapLayer)
            }
            mapRenderer!!.batch.end()
            mapMgr.updateCurrentMapEntities(mapMgr, mapRenderer!!.batch, delta)
            player!!.update(mapMgr, mapRenderer!!.batch, delta)
            mapMgr.updateCurrentMapEffects(mapMgr, mapRenderer!!.batch, delta)
            mapRenderer!!.batch.begin()
            mapRenderer!!.batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ONE_MINUS_SRC_ALPHA)
            mapRenderer!!.renderImageLayer(lightMap)
            mapRenderer!!.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
            mapRenderer!!.batch.end()
            if (previousLightMap != null) {
                mapRenderer!!.batch.begin()
                mapRenderer!!.batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ONE_MINUS_SRC_COLOR)
                mapRenderer!!.renderImageLayer(previousLightMap)
                mapRenderer!!.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
                mapRenderer!!.batch.end()
            }
        } else {
            mapRenderer!!.render()
            mapMgr.updateCurrentMapEntities(mapMgr, mapRenderer!!.batch, delta)
            player!!.update(mapMgr, mapRenderer!!.batch, delta)
            mapMgr.updateCurrentMapEffects(mapMgr, mapRenderer!!.batch, delta)
        }
        playerHUD.render(delta)
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun resume() {
        TODO("Not yet implemented")
    }

    override fun resize(width: Int, height: Int) {
        TODO("Not yet implemented")
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }
}