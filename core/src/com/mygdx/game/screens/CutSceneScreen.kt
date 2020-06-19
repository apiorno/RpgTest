package com.mygdx.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.mygdx.game.*
import com.mygdx.game.BludBourne.ScreenType
import com.mygdx.game.ecs.EntityFactory.Companion.instance
import com.mygdx.game.ecs.EntityFactory.EntityName
import com.mygdx.game.maps.Map
import com.mygdx.game.widgets.AnimatedImage
import com.mygdx.game.audio.AudioObserver.AudioCommand
import com.mygdx.game.audio.AudioObserver.AudioTypeEvent
import com.mygdx.game.battle.MonsterFactory
import com.mygdx.game.battle.MonsterFactory.MonsterEntityType
import com.mygdx.game.ecs.Entity
import com.mygdx.game.maps.MapFactory
import com.mygdx.game.profile.ProfileManager
import com.mygdx.game.sfx.ScreenTransitionAction
import com.mygdx.game.sfx.ScreenTransitionAction.Companion.transition
import com.mygdx.game.sfx.ScreenTransitionActor

class CutSceneScreen(private val _game: BludBourne) : MainGameScreen(_game) {
    private val _stage: Stage
    private val _viewport: Viewport
    private val _UIStage: Stage
    private val _UIViewport: Viewport
    private var _followingActor: Actor
    private val _messageBoxUI: Dialog
    private val _label: Label
    private var _isCameraFixed = true
    private val _transitionActor: ScreenTransitionActor
    private var _introCutSceneAction: Action? = null
    private val _switchScreenAction: Action
    private val _setupScene01: Action
    private val _setupScene02: Action
    private val _setupScene03: Action
    private val _setupScene04: Action
    private val _setupScene05: Action
    private val _animBlackSmith: AnimatedImage
    private val _animInnKeeper: AnimatedImage
    private val _animMage: AnimatedImage
    private val _animFire: AnimatedImage
    private val _animDemon: AnimatedImage
    private val cutsceneAction: Action
        private get() {
            _setupScene01.reset()
            _setupScene02.reset()
            _setupScene03.reset()
            _setupScene04.reset()
            _setupScene05.reset()
            _switchScreenAction.reset()
            return Actions.sequence(
                    Actions.addAction(_setupScene01),
                    Actions.addAction(transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 3f), _transitionActor),
                    Actions.delay(3f),
                    Actions.run { showMessage("BLACKSMITH: We have planned this long enough. The time is now! I have had enough talk...") },
                    Actions.delay(7f),
                    Actions.run { showMessage("MAGE: This is dark magic you fool. We must proceed with caution, or this could end badly for all of us") },
                    Actions.delay(7f),
                    Actions.run { showMessage("INNKEEPER: Both of you need to keep it down. If we get caught using black magic, we will all be hanged!") },
                    Actions.delay(5f),
                    Actions.addAction(transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 3f), _transitionActor),
                    Actions.delay(3f),
                    Actions.addAction(_setupScene02),
                    Actions.addAction(transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 3f), _transitionActor),
                    Actions.delay(3f),
                    Actions.run { showMessage("BLACKSMITH: Now, let's get on with this. I don't like the cemeteries very much...") },
                    Actions.delay(7f),
                    Actions.run { showMessage("MAGE: I told you, we can't rush the spell. Bringing someone back to life isn't simple!") },
                    Actions.delay(7f),
                    Actions.run { showMessage("INNKEEPER: I know you loved your daughter, but this just isn't right...") },
                    Actions.delay(7f),
                    Actions.run { showMessage("BLACKSMITH: You have never had a child of your own. You just don't understand!") },
                    Actions.delay(7f),
                    Actions.run { showMessage("MAGE: You both need to concentrate, wait...Oh no, something is wrong!!") },
                    Actions.delay(7f),
                    Actions.addAction(_setupScene03),
                    Actions.addAction(Actions.fadeOut(2f), _animDemon),
                    Actions.delay(2f),
                    Actions.addAction(Actions.fadeIn(2f), _animDemon),
                    Actions.delay(2f),
                    Actions.addAction(Actions.fadeOut(2f), _animDemon),
                    Actions.delay(2f),
                    Actions.addAction(Actions.fadeIn(2f), _animDemon),
                    Actions.delay(2f),
                    Actions.addAction(Actions.fadeOut(2f), _animDemon),
                    Actions.delay(2f),
                    Actions.addAction(Actions.fadeIn(2f), _animDemon),
                    Actions.delay(2f),
                    Actions.addAction(Actions.scaleBy(40f, 40f, 5f, Interpolation.linear), _animDemon),
                    Actions.delay(5f),
                    Actions.addAction(Actions.moveBy(20f, 0f), _animDemon),
                    Actions.delay(2f),
                    Actions.run { showMessage("BLACKSMITH: What...What have we done...") },
                    Actions.delay(3f),
                    Actions.addAction(transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 3f), _transitionActor),
                    Actions.delay(3f),
                    Actions.addAction(_setupScene04),
                    Actions.addAction(transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 3f), _transitionActor),
                    Actions.addAction(Actions.moveTo(54f, 65f, 13f, Interpolation.linear), _animDemon),
                    Actions.delay(10f),
                    Actions.addAction(transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 3f), _transitionActor),
                    Actions.delay(3f),
                    Actions.addAction(transition(ScreenTransitionAction.ScreenTransitionType.FADE_IN, 3f), _transitionActor),
                    Actions.addAction(_setupScene05),
                    Actions.addAction(Actions.moveTo(15f, 76f, 15f, Interpolation.linear), _animDemon),
                    Actions.delay(15f),
                    Actions.run { showMessage("DEMON: I will now send my legions of demons to destroy these sacks of meat!") },
                    Actions.delay(5f),
                    Actions.addAction(transition(ScreenTransitionAction.ScreenTransitionType.FADE_OUT, 3f), _transitionActor),
                    Actions.delay(5f),
                    Actions.after(_switchScreenAction)
            )
        }

    private fun getAnimatedImage(entityName: EntityName): AnimatedImage {
        val entity = instance!!.getEntityByName(entityName)
        return setEntityAnimation(entity)
    }

    private fun getAnimatedImage(entityName: MonsterEntityType): AnimatedImage {
        val entity = MonsterFactory.instance.getMonster(entityName)
        return setEntityAnimation(entity)
    }

    private fun setEntityAnimation(entity: Entity?): AnimatedImage {
        val animEntity = AnimatedImage()
        animEntity.setEntity(entity)
        animEntity.setSize(animEntity.width * Map.UNIT_SCALE, animEntity.height * Map.UNIT_SCALE)
        return animEntity
    }

    fun followActor(actor: Actor) {
        _followingActor = actor
        _isCameraFixed = false
    }

    fun setCameraPosition(x: Float, y: Float) {
        _camera!!.position[x, y] = 0f
        _isCameraFixed = true
    }

    fun showMessage(message: String?) {
        _label.setText(message)
        _messageBoxUI.pack()
        _messageBoxUI.isVisible = true
    }

    fun hideMessage() {
        _messageBoxUI.isVisible = false
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        _mapRenderer!!.setView(_camera)
        _mapRenderer!!.batch.enableBlending()
        _mapRenderer!!.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        if (_mapMgr.hasMapChanged()) {
            _mapRenderer!!.map = _mapMgr.currentTiledMap
            _mapMgr.setMapChanged(false)
        }
        _mapRenderer!!.render()
        if (!_isCameraFixed) {
            _camera!!.position[_followingActor.x, _followingActor.y] = 0f
        }
        _camera!!.update()
        _UIStage.act(delta)
        _UIStage.draw()
        _stage.act(delta)
        _stage.draw()
    }

    override fun show() {
        _introCutSceneAction = cutsceneAction
        _stage.addAction(_introCutSceneAction)
        notify(AudioCommand.MUSIC_STOP_ALL, AudioTypeEvent.NONE)
        notify(AudioCommand.MUSIC_PLAY_LOOP, AudioTypeEvent.MUSIC_INTRO_CUTSCENE)
        ProfileManager.instance.removeAllObservers()
        if (_mapRenderer == null) {
            _mapRenderer = OrthogonalTiledMapRenderer(_mapMgr.currentTiledMap, Map.UNIT_SCALE)
        }
    }

    override fun hide() {
        notify(AudioCommand.MUSIC_STOP, AudioTypeEvent.MUSIC_INTRO_CUTSCENE)
        ProfileManager.instance.removeAllObservers()
        Gdx.input.inputProcessor = null
    }

    init {
        _viewport = ScreenViewport(_camera)
        _stage = Stage(_viewport)
        _UIViewport = ScreenViewport(_hudCamera)
        _UIStage = Stage(_UIViewport)
        _label = Label("Test", Utility.STATUSUI_SKIN)
        _label.setWrap(true)
        _messageBoxUI = Dialog("", Utility.STATUSUI_SKIN, "solidbackground")
        _messageBoxUI.isVisible = false
        _messageBoxUI.contentTable.add(_label).width(_stage.width / 2).pad(10f, 10f, 10f, 0f)
        _messageBoxUI.pack()
        _messageBoxUI.setPosition(_stage.width / 2 - _messageBoxUI.width / 2, _stage.height - _messageBoxUI.height)
        _followingActor = Actor()
        _followingActor.setPosition(0f, 0f)
        notify(AudioCommand.MUSIC_LOAD, AudioTypeEvent.MUSIC_INTRO_CUTSCENE)
        _animBlackSmith = getAnimatedImage(EntityName.TOWN_BLACKSMITH)
        _animInnKeeper = getAnimatedImage(EntityName.TOWN_INNKEEPER)
        _animMage = getAnimatedImage(EntityName.TOWN_MAGE)
        _animFire = getAnimatedImage(EntityName.FIRE)
        _animDemon = getAnimatedImage(MonsterEntityType.MONSTER042)

        //Actions
        _switchScreenAction = object : RunnableAction() {
            override fun run() {
                _game.screen = _game.getScreenType(ScreenType.MainMenu)
            }
        }
        _setupScene01 = object : RunnableAction() {
            override fun run() {
                hideMessage()
                _mapMgr.loadMap(MapFactory.MapType.TOWN)
                _mapMgr.disableCurrentmapMusic()
                setCameraPosition(10f, 16f)
                _animBlackSmith.isVisible = true
                _animInnKeeper.isVisible = true
                _animMage.isVisible = true
                _animBlackSmith.setPosition(10f, 16f)
                _animInnKeeper.setPosition(12f, 15f)
                _animMage.setPosition(11f, 17f)
                _animDemon.isVisible = false
                _animFire.isVisible = false
            }
        }
        _setupScene02 = object : RunnableAction() {
            override fun run() {
                hideMessage()
                _mapMgr.loadMap(MapFactory.MapType.TOP_WORLD)
                _mapMgr.disableCurrentmapMusic()
                setCameraPosition(50f, 30f)
                _animBlackSmith.setPosition(50f, 30f)
                _animInnKeeper.setPosition(52f, 30f)
                _animMage.setPosition(50f, 28f)
                _animFire.setPosition(52f, 28f)
                _animFire.isVisible = true
            }
        }
        _setupScene03 = object : RunnableAction() {
            override fun run() {
                _animDemon.setPosition(52f, 28f)
                _animDemon.isVisible = true
                hideMessage()
            }
        }
        _setupScene04 = object : RunnableAction() {
            override fun run() {
                hideMessage()
                _animBlackSmith.isVisible = false
                _animInnKeeper.isVisible = false
                _animMage.isVisible = false
                _animFire.isVisible = false
                _mapMgr.loadMap(MapFactory.MapType.TOP_WORLD)
                _mapMgr.disableCurrentmapMusic()
                _animDemon.isVisible = true
                _animDemon.setScale(1f, 1f)
                _animDemon.setSize(16 * Map.UNIT_SCALE, 16 * Map.UNIT_SCALE)
                _animDemon.setPosition(50f, 40f)
                followActor(_animDemon)
            }
        }
        _setupScene05 = object : RunnableAction() {
            override fun run() {
                hideMessage()
                _animBlackSmith.isVisible = false
                _animInnKeeper.isVisible = false
                _animMage.isVisible = false
                _animFire.isVisible = false
                _mapMgr.loadMap(MapFactory.MapType.CASTLE_OF_DOOM)
                _mapMgr.disableCurrentmapMusic()
                followActor(_animDemon)
                _animDemon.isVisible = true
                _animDemon.setPosition(15f, 1f)
            }
        }
        _transitionActor = ScreenTransitionActor()

        //layout
        _stage.addActor(_animMage)
        _stage.addActor(_animBlackSmith)
        _stage.addActor(_animInnKeeper)
        _stage.addActor(_animFire)
        _stage.addActor(_animDemon)
        _stage.addActor(_transitionActor)
        _UIStage.addActor(_messageBoxUI)
    }
}