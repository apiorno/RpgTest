package com.mygdx.game.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.Json
import com.mygdx.game.EntityConfig
import com.mygdx.game.MyGdxGame
import com.mygdx.game.ScreenManager.*
import com.mygdx.game.Systems
import com.mygdx.game.maps.MapManager
import com.mygdx.game.profile.ProfileManager
import com.mygdx.game.temporal.EntityFactory
import com.mygdx.game.widgets.PlayerHUD

open class MainGameScreen  (private val game: MyGdxGame) : GameScreen() {
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
    protected var mapMgr: MapManager = MapManager()

    protected var hudCamera: OrthographicCamera? = null
    private val json: Json = Json()
    private val multiplexer: InputMultiplexer
    private val playerHUD: PlayerHUD
    val camera : OrthographicCamera
    private val playerConfig: EntityConfig

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
        setGameState(GameState.RUNNING)

        //_camera setup
        setupViewport(10, 10)

        //get the current size
        camera = game.injector.getInstance(OrthographicCamera::class.java)
        camera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight)
        playerConfig = EntityFactory.instance!!.getEntityConfigByName(EntityFactory.EntityName.PLAYER_PUPPET)
        mapMgr.playerConfig = playerConfig
        mapMgr.camera = camera
        hudCamera = OrthographicCamera()
        hudCamera!!.setToOrtho(false, VIEWPORT.physicalWidth, VIEWPORT.physicalHeight)
        playerHUD = PlayerHUD(hudCamera!!, playerConfig, mapMgr)
        multiplexer = InputMultiplexer()
        multiplexer.addProcessor(playerHUD.stage)
        multiplexer.addProcessor(game.inputProcessor)
        Gdx.input.inputProcessor = multiplexer
    }

    private fun setupViewport(width: Int, height: Int) {
        //Make the viewport a percentage of the total display area
        VIEWPORT.virtualWidth = width.toFloat()
        VIEWPORT.virtualHeight = height.toFloat()

        //Current viewport dimensions
        VIEWPORT.viewportWidth = VIEWPORT.virtualWidth
        VIEWPORT.viewportHeight = VIEWPORT.virtualHeight

        //pixel dimensions of display
        VIEWPORT.physicalWidth = Gdx.graphics.width.toFloat()
        VIEWPORT.physicalHeight = Gdx.graphics.height.toFloat()

        //aspect ratio for current viewport
        VIEWPORT.aspectRatio = VIEWPORT.virtualWidth / VIEWPORT.virtualHeight

        //update viewport if there could be skewing
        if (VIEWPORT.physicalWidth / VIEWPORT.physicalHeight >= VIEWPORT.aspectRatio) {
            //Letterbox left and right
            VIEWPORT.viewportWidth = VIEWPORT.viewportHeight * (VIEWPORT.physicalWidth / VIEWPORT.physicalHeight)
            VIEWPORT.viewportHeight = VIEWPORT.virtualHeight
        } else {
            //letterbox above and below
            VIEWPORT.viewportWidth = VIEWPORT.virtualWidth
            VIEWPORT.viewportHeight = VIEWPORT.viewportWidth * (VIEWPORT.physicalHeight / VIEWPORT.physicalWidth)
        }
        Gdx.app.debug(TAG, "WorldRenderer: virtual: (" + VIEWPORT.virtualWidth + "," + VIEWPORT.virtualHeight + ")")
        Gdx.app.debug(TAG, "WorldRenderer: viewport: (" + VIEWPORT.viewportWidth + "," + VIEWPORT.viewportHeight + ")")
        Gdx.app.debug(TAG, "WorldRenderer: physical: (" + VIEWPORT.physicalWidth + "," + VIEWPORT.physicalHeight + ")")
    }

    override fun hide() {
    }

    override fun show() {

        createEntities()
    }

    private fun createEntities() {
        game.engine.addEntity(EntityFactory.instance?.getPlayer())
    }

    override fun render(delta: Float) {
        /*if (gameState == GameState.GAME_OVER) {
            game.setScreenOfType(ScreenType.GameOver)
        }
        if (gameState == GameState.PAUSED) {
            //playerConfig.updateInput(delta)
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
        mapMgr.renderMap()*/
        game.engine.update(delta)
        playerHUD.render(delta)
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun dispose() {
    }
}