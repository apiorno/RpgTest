package com.mygdx.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.utils.Json
import com.mygdx.game.*
import com.mygdx.game.ScreenManager.*
import com.mygdx.game.ecs.EntityFactory.Companion.getEntity
import com.mygdx.game.maps.Map
import com.mygdx.game.maps.MapFactory.clearCache
import com.mygdx.game.widgets.PlayerHUD
import com.mygdx.game.audio.AudioManager
import com.mygdx.game.ecs.Component
import com.mygdx.game.ecs.Entity
import com.mygdx.game.ecs.EntityFactory
import com.mygdx.game.maps.MapManager
import com.mygdx.game.profile.ProfileManager

open class MainGameScreen(private val _game: BludBourne) : GameScreen() {
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

    protected var _mapRenderer: OrthogonalTiledMapRenderer? = null
    protected var _mapMgr: MapManager
    protected var _camera: OrthographicCamera? = null
    protected var _hudCamera: OrthographicCamera? = null
    private val _json: Json
    private val _multiplexer: InputMultiplexer
    private val _player: Entity?
    private val _playerHUD: PlayerHUD
    override fun show() {
        ProfileManager.addObserver(_mapMgr)
        ProfileManager.addObserver(_playerHUD)
        setGameState(GameState.LOADING)
        Gdx.input.inputProcessor = _multiplexer
        if (_mapRenderer == null) {
            _mapRenderer = OrthogonalTiledMapRenderer(_mapMgr.currentTiledMap, Map.UNIT_SCALE)
        }
    }

    override fun hide() {
        if (_gameState != GameState.GAME_OVER) {
            setGameState(GameState.SAVING)
        }
        Gdx.input.inputProcessor = null
    }

    override fun render(delta: Float) {
        if (_gameState == GameState.GAME_OVER) {
            _game.changeScreenToType(ScreenType.GameOver)
        }
        if (_gameState == GameState.PAUSED) {
            _player!!.updateInput(delta)
            _playerHUD.render(delta)
            return
        }
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        _mapRenderer!!.setView(_camera)
        _mapRenderer!!.batch.enableBlending()
        _mapRenderer!!.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        if (_mapMgr.hasMapChanged()) {
            _mapRenderer!!.map = _mapMgr.currentTiledMap
            _player!!.sendMessage(Component.MESSAGE.INIT_START_POSITION, _json.toJson(_mapMgr.playerStartUnitScaled))
            _camera!!.position[_mapMgr.playerStartUnitScaled!!.x, _mapMgr.playerStartUnitScaled!!.y] = 0f
            _camera!!.update()
            _playerHUD.updateEntityObservers()
            _mapMgr.setMapChanged(false)
            _playerHUD.addTransitionToScreen()
        }
        _mapMgr.updateLightMaps(_playerHUD.currentTimeOfDay)
        val lightMap = _mapMgr.currentLightMapLayer as TiledMapImageLayer?
        val previousLightMap = _mapMgr.previousLightMapLayer as TiledMapImageLayer?
        if (lightMap != null) {
            _mapRenderer!!.batch.begin()
            val backgroundMapLayer = _mapMgr.currentTiledMap!!.layers[Map.BACKGROUND_LAYER] as TiledMapTileLayer
            if (backgroundMapLayer != null) {
                _mapRenderer!!.renderTileLayer(backgroundMapLayer)
            }
            val groundMapLayer = _mapMgr.currentTiledMap!!.layers[Map.GROUND_LAYER] as TiledMapTileLayer
            if (groundMapLayer != null) {
                _mapRenderer!!.renderTileLayer(groundMapLayer)
            }
            val decorationMapLayer = _mapMgr.currentTiledMap!!.layers[Map.DECORATION_LAYER] as TiledMapTileLayer
            if (decorationMapLayer != null) {
                _mapRenderer!!.renderTileLayer(decorationMapLayer)
            }
            _mapRenderer!!.batch.end()
            _mapMgr.updateCurrentMapEntities(_mapMgr, _mapRenderer!!.batch, delta)
            _player!!.update(_mapMgr, _mapRenderer!!.batch, delta)
            _mapMgr.updateCurrentMapEffects(_mapMgr, _mapRenderer!!.batch, delta)
            _mapRenderer!!.batch.begin()
            _mapRenderer!!.batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ONE_MINUS_SRC_ALPHA)
            _mapRenderer!!.renderImageLayer(lightMap)
            _mapRenderer!!.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
            _mapRenderer!!.batch.end()
            if (previousLightMap != null) {
                _mapRenderer!!.batch.begin()
                _mapRenderer!!.batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ONE_MINUS_SRC_COLOR)
                _mapRenderer!!.renderImageLayer(previousLightMap)
                _mapRenderer!!.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
                _mapRenderer!!.batch.end()
            }
        } else {
            _mapRenderer!!.render()
            _mapMgr.updateCurrentMapEntities(_mapMgr, _mapRenderer!!.batch, delta)
            _player!!.update(_mapMgr, _mapRenderer!!.batch, delta)
            _mapMgr.updateCurrentMapEffects(_mapMgr, _mapRenderer!!.batch, delta)
        }
        _playerHUD.render(delta)
    }

    override fun resize(width: Int, height: Int) {
        setupViewport(10, 10)
        _camera!!.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight)
        _playerHUD.resize(VIEWPORT.physicalWidth.toInt(), VIEWPORT.physicalHeight.toInt())
    }

    override fun pause() {
        setGameState(GameState.SAVING)
        _playerHUD.pause()
    }

    override fun resume() {
        setGameState(GameState.LOADING)
        _playerHUD.resume()
    }

    override fun dispose() {
        if (_player != null) {
            _player.unregisterObservers()
            _player.dispose()
        }
        if (_mapRenderer != null) {
            _mapRenderer!!.dispose()
        }
        AudioManager.dispose()
        clearCache()
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

    companion object {
        private val TAG = MainGameScreen::class.java.simpleName
        private var _gameState: GameState? = null
        @JvmStatic
		fun setGameState(gameState: GameState?) {
            when (gameState) {
                GameState.RUNNING -> _gameState = GameState.RUNNING
                GameState.LOADING -> {
                    ProfileManager.loadProfile()
                    _gameState = GameState.RUNNING
                }
                GameState.SAVING -> {
                    ProfileManager.saveProfile()
                    _gameState = GameState.PAUSED
                }
                GameState.PAUSED -> if (_gameState == GameState.PAUSED) {
                    _gameState = GameState.RUNNING
                } else if (_gameState == GameState.RUNNING) {
                    _gameState = GameState.PAUSED
                }
                GameState.GAME_OVER -> _gameState = GameState.GAME_OVER
                else -> _gameState = GameState.RUNNING
            }
        }
    }

    init {
        _mapMgr = MapManager()
        _json = Json()
        setGameState(GameState.RUNNING)

        //_camera setup
        setupViewport(10, 10)

        //get the current size
        _camera = OrthographicCamera()
        _camera!!.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight)
        _player = getEntity(EntityFactory.EntityType.PLAYER)
        _mapMgr.player = _player!!
        _mapMgr.camera = _camera!!
        _hudCamera = OrthographicCamera()
        _hudCamera!!.setToOrtho(false, VIEWPORT.physicalWidth, VIEWPORT.physicalHeight)
        _playerHUD = PlayerHUD(_hudCamera!!, _player, _mapMgr)
        _multiplexer = InputMultiplexer()
        _multiplexer.addProcessor(_playerHUD.stage)
        _multiplexer.addProcessor(_player.inputProcessor)
        Gdx.input.inputProcessor = _multiplexer

        //Gdx.app.debug(TAG, "UnitScale value is: " + _mapRenderer.getUnitScale());
    }
}