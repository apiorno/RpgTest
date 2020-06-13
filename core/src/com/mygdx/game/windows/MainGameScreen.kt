package com.mygdx.game.windows

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.utils.Json
import com.mygdx.game.MyGdxGame
import com.mygdx.game.ScreenManager.*
import com.mygdx.game.maps.MapManager
import com.mygdx.game.profile.ProfileManager
import com.mygdx.game.temporal.EntityFactory

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
    private val camera : OrthographicCamera
    private val player: Entity

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
        camera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight)
        player = EntityFactory.instance!!.getPlayer()!!
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
            player.updateInput(delta)
            playerHUD.render(delta)
            return
        }
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        mapMgr.preRenderMap()
        if (mapMgr.hasMapChanged()) {
            mapMgr.changeRendererMap()
            //player.sendMessage(MESSAGE.INIT_START_POSITION, json.toJson(mapMgr.playerStartUnitScaled))
            camera.position[mapMgr.playerStartUnitScaled!!.x, mapMgr.playerStartUnitScaled!!.y] = 0f
            camera.update()
            playerHUD.updateEntityObservers()
            mapMgr.setMapChanged(false)
            playerHUD.addTransitionToScreen()
        }
        mapMgr.renderMap()
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