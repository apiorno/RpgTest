package com.mygdx.game.windows

import UNIT_SCALE
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
import com.mygdx.game.EntityConfig
import com.mygdx.game.MyGdxGame
import com.mygdx.game.ScreenManager.*
import com.mygdx.game.Utility
import com.mygdx.game.audio.AudioObserver
import com.mygdx.game.profile.ProfileManager
import com.mygdx.game.sfx.ScreenTransitionAction.*
import com.mygdx.game.sfx.ScreenTransitionAction.Companion.transition
import com.mygdx.game.sfx.ScreenTransitionActor
import com.mygdx.game.temporal.EntityFactory
import com.mygdx.game.temporal.EntityFactory.*
import com.mygdx.game.temporal.MonsterFactory
import com.mygdx.game.temporal.MonsterFactory.*
import com.mygdx.game.widgets.AnimatedImage

class CutSceneScreen(private val game: MyGdxGame) : MainGameScreen(game) {
    private val stage: Stage
    private val viewport: Viewport
    private val UIStage: Stage
    private val UIViewport: Viewport
    private var followingActor: Actor
    private val messageBoxUI: Dialog
    private val label: Label
    private var isCameraFixed = true
    private val transitionActor: ScreenTransitionActor
    private var introCutSceneAction: Action? = null
    private val switchScreenAction: Action
    private val setupScene01: Action
    private val setupScene02: Action
    private val setupScene03: Action
    private val setupScene04: Action
    private val setupScene05: Action
    private val animBlackSmith: AnimatedImage
    private val animInnKeeper: AnimatedImage
    private val animMage: AnimatedImage
    private val animFire: AnimatedImage
    private val animDemon: AnimatedImage
    private val cutsceneAction: Action
        private get() {
            setupScene01.reset()
            setupScene02.reset()
            setupScene03.reset()
            setupScene04.reset()
            setupScene05.reset()
            switchScreenAction.reset()
            return Actions.sequence(
                    Actions.addAction(setupScene01),
                    Actions.addAction(transition(ScreenTransitionType.FADE_IN, 3f), transitionActor),
                    Actions.delay(3f),
                    Actions.run { showMessage("BLACKSMITH: We have planned this long enough. The time is now! I have had enough talk...") },
                    Actions.delay(7f),
                    Actions.run { showMessage("MAGE: This is dark magic you fool. We must proceed with caution, or this could end badly for all of us") },
                    Actions.delay(7f),
                    Actions.run { showMessage("INNKEEPER: Both of you need to keep it down. If we get caught using black magic, we will all be hanged!") },
                    Actions.delay(5f),
                    Actions.addAction(transition(ScreenTransitionType.FADE_OUT, 3f), transitionActor),
                    Actions.delay(3f),
                    Actions.addAction(setupScene02),
                    Actions.addAction(transition(ScreenTransitionType.FADE_IN, 3f), transitionActor),
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
                    Actions.addAction(setupScene03),
                    Actions.addAction(Actions.fadeOut(2f), animDemon),
                    Actions.delay(2f),
                    Actions.addAction(Actions.fadeIn(2f), animDemon),
                    Actions.delay(2f),
                    Actions.addAction(Actions.fadeOut(2f), animDemon),
                    Actions.delay(2f),
                    Actions.addAction(Actions.fadeIn(2f), animDemon),
                    Actions.delay(2f),
                    Actions.addAction(Actions.fadeOut(2f), animDemon),
                    Actions.delay(2f),
                    Actions.addAction(Actions.fadeIn(2f), animDemon),
                    Actions.delay(2f),
                    Actions.addAction(Actions.scaleBy(40f, 40f, 5f, Interpolation.linear), animDemon),
                    Actions.delay(5f),
                    Actions.addAction(Actions.moveBy(20f, 0f), animDemon),
                    Actions.delay(2f),
                    Actions.run { showMessage("BLACKSMITH: What...What have we done...") },
                    Actions.delay(3f),
                    Actions.addAction(transition(ScreenTransitionType.FADE_OUT, 3f), transitionActor),
                    Actions.delay(3f),
                    Actions.addAction(setupScene04),
                    Actions.addAction(transition(ScreenTransitionType.FADE_IN, 3f), transitionActor),
                    Actions.addAction(Actions.moveTo(54f, 65f, 13f, Interpolation.linear), animDemon),
                    Actions.delay(10f),
                    Actions.addAction(transition(ScreenTransitionType.FADE_OUT, 3f), transitionActor),
                    Actions.delay(3f),
                    Actions.addAction(transition(ScreenTransitionType.FADE_IN, 3f), transitionActor),
                    Actions.addAction(setupScene05),
                    Actions.addAction(Actions.moveTo(15f, 76f, 15f, Interpolation.linear), animDemon),
                    Actions.delay(15f),
                    Actions.run { showMessage("DEMON: I will now send my legions of demons to destroy these sacks of meat!") },
                    Actions.delay(5f),
                    Actions.addAction(transition(ScreenTransitionType.FADE_OUT, 3f), transitionActor),
                    Actions.delay(5f),
                    Actions.after(switchScreenAction)
            )
        }

    private fun getAnimatedImage(entityName: EntityName): AnimatedImage {
        val entityConfig = EntityFactory.instance!!.getEntityConfigByName(entityName)
        return setEntityAnimation(entityConfig)
    }

    private fun getAnimatedImage(entityName: MonsterEntityType): AnimatedImage {
        val entityConfig = MonsterFactory.instance!!.getMonsterConfigByType(entityName)
        return setEntityAnimation(entityConfig!!)
    }

    private fun setEntityAnimation(entityConfig: EntityConfig): AnimatedImage {
        val animEntity = AnimatedImage()
        animEntity.setEntityConfig(entityConfig)
        animEntity.setSize(animEntity.width * UNIT_SCALE, animEntity.height * UNIT_SCALE)
        return animEntity
    }

    fun followActor(actor: Actor) {
        followingActor = actor
        isCameraFixed = false
    }

    fun setCameraPosition(x: Float, y: Float) {
        camera!!.position[x, y] = 0f
        isCameraFixed = true
    }

    fun showMessage(message: String?) {
        label.setText(message)
        messageBoxUI.pack()
        messageBoxUI.isVisible = true
    }

    fun hideMessage() {
        messageBoxUI.isVisible = false
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        mapRenderer!!.setView(camera)
        mapRenderer!!.batch.enableBlending()
        mapRenderer!!.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        if (mapMgr.hasMapChanged()) {
            mapRenderer!!.map = mapMgr.currentTiledMap
            mapMgr.setMapChanged(false)
        }
        mapRenderer!!.render()
        if (!isCameraFixed) {
            camera!!.position[followingActor.x, followingActor.y] = 0f
        }
        camera!!.update()
        UIStage.act(delta)
        UIStage.draw()
        stage.act(delta)
        stage.draw()
    }

    override fun show() {
        introCutSceneAction = cutsceneAction
        stage.addAction(introCutSceneAction)
        notify(AudioObserver.AudioCommand.MUSIC_STOP_ALL, AudioObserver.AudioTypeEvent.NONE)
        notify(AudioObserver.AudioCommand.MUSIC_PLAY_LOOP, AudioObserver.AudioTypeEvent.MUSIC_INTRO_CUTSCENE)
        ProfileManager.instance.removeAllObservers()
        if (mapRenderer == null) {
            mapRenderer = OrthogonalTiledMapRenderer(mapMgr.currentTiledMap, UNIT_SCALE)
        }
    }

    override fun hide() {
        notify(AudioObserver.AudioCommand.MUSIC_STOP, AudioObserver.AudioTypeEvent.MUSIC_INTRO_CUTSCENE)
        ProfileManager.instance.removeAllObservers()
        Gdx.input.inputProcessor = null
    }

    init {
        viewport = ScreenViewport(camera)
        stage = Stage(viewport)
        UIViewport = ScreenViewport(hudCamera)
        UIStage = Stage(UIViewport)
        label = Label("Test", Utility.STATUSUI_SKIN)
        label.setWrap(true)
        messageBoxUI = Dialog("", Utility.STATUSUI_SKIN, "solidbackground")
        messageBoxUI.isVisible = false
        messageBoxUI.contentTable.add(label).width(stage.width / 2).pad(10f, 10f, 10f, 0f)
        messageBoxUI.pack()
        messageBoxUI.setPosition(stage.width / 2 - messageBoxUI.width / 2, stage.height - messageBoxUI.height)
        followingActor = Actor()
        followingActor.setPosition(0f, 0f)
        notify(AudioObserver.AudioCommand.MUSIC_LOAD, AudioObserver.AudioTypeEvent.MUSIC_INTRO_CUTSCENE)
        animBlackSmith = getAnimatedImage(EntityName.TOWN_BLACKSMITH)
        animInnKeeper = getAnimatedImage(EntityName.TOWN_INNKEEPER)
        animMage = getAnimatedImage(EntityName.TOWN_MAGE)
        animFire = getAnimatedImage(EntityName.FIRE)
        animDemon = getAnimatedImage(MonsterEntityType.MONSTER042)

        //Actions
        switchScreenAction = object : RunnableAction() {
            override fun run() {
                game.setScreenOfType(ScreenType.MainMenu)
            }
        }
        setupScene01 = object : RunnableAction() {
            override fun run() {
                hideMessage()
                mapMgr.loadMap(MapFactory.MapType.TOWN)
                mapMgr.disableCurrentmapMusic()
                setCameraPosition(10f, 16f)
                animBlackSmith.isVisible = true
                animInnKeeper.isVisible = true
                animMage.isVisible = true
                animBlackSmith.setPosition(10f, 16f)
                animInnKeeper.setPosition(12f, 15f)
                animMage.setPosition(11f, 17f)
                animDemon.isVisible = false
                animFire.isVisible = false
            }
        }
        setupScene02 = object : RunnableAction() {
            override fun run() {
                hideMessage()
                mapMgr.loadMap(MapFactory.MapType.TOP_WORLD)
                mapMgr.disableCurrentmapMusic()
                setCameraPosition(50f, 30f)
                animBlackSmith.setPosition(50f, 30f)
                animInnKeeper.setPosition(52f, 30f)
                animMage.setPosition(50f, 28f)
                animFire.setPosition(52f, 28f)
                animFire.isVisible = true
            }
        }
        setupScene03 = object : RunnableAction() {
            override fun run() {
                animDemon.setPosition(52f, 28f)
                animDemon.isVisible = true
                hideMessage()
            }
        }
        setupScene04 = object : RunnableAction() {
            override fun run() {
                hideMessage()
                animBlackSmith.isVisible = false
                animInnKeeper.isVisible = false
                animMage.isVisible = false
                animFire.isVisible = false
                mapMgr.loadMap(MapFactory.MapType.TOP_WORLD)
                mapMgr.disableCurrentmapMusic()
                animDemon.isVisible = true
                animDemon.setScale(1f, 1f)
                animDemon.setSize(16 * UNIT_SCALE, 16 * UNIT_SCALE)
                animDemon.setPosition(50f, 40f)
                followActor(animDemon)
            }
        }
        setupScene05 = object : RunnableAction() {
            override fun run() {
                hideMessage()
                animBlackSmith.isVisible = false
                animInnKeeper.isVisible = false
                animMage.isVisible = false
                animFire.isVisible = false
                mapMgr.loadMap(MapFactory.MapType.CASTLE_OF_DOOM)
                mapMgr.disableCurrentmapMusic()
                followActor(animDemon)
                animDemon.isVisible = true
                animDemon.setPosition(15f, 1f)
            }
        }
        transitionActor = ScreenTransitionActor()

        //layout
        stage.addActor(animMage)
        stage.addActor(animBlackSmith)
        stage.addActor(animInnKeeper)
        stage.addActor(animFire)
        stage.addActor(animDemon)
        stage.addActor(transitionActor)
        UIStage.addActor(messageBoxUI)
    }
}