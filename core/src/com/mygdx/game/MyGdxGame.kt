package com.mygdx.game

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.GridPoint2
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.Array
import com.google.inject.*
import com.mygdx.game.systems.InputHandlerSystem
import com.mygdx.game.systems.TransformSystem
import java.util.*

class MyGdxGame : Game() {
    lateinit var batch: SpriteBatch
    lateinit var engine : Engine
    private val screnManager = ScreenManager(this)
    lateinit var inputProcessor: PlayerInputProcessor
    private lateinit var injector : Injector
    companion object{
        lateinit var img: Texture
    }
    @Override
    override fun create() {
        batch = SpriteBatch()
        setScreen(screnManager.getScreenType(ScreenManager.ScreenType.MainMenu))
        injector = Guice.createInjector(GameModule(this))
        inputProcessor = injector.getInstance(PlayerInputProcessor::class.java)
        engine = injector.getInstance(Engine::class.java)
        injector.getInstance(Systems::class.java).list.map { injector.getInstance(it) }.forEach{system -> engine.addSystem(system)}
        inputProcessor.add(engine.getSystem(InputHandlerSystem::class.java))
        inputProcessor.add(engine.getSystem(TransformSystem::class.java))
        Gdx.input.inputProcessor = inputProcessor
        createEntities()
    }
    fun setScreenOfType(screenType : ScreenManager.ScreenType){
        setScreen(screnManager.getScreenType(screenType))
    }

    private fun createEntities() {
        val world = injector.getInstance(World::class.java)
        var animations: Hashtable<String, Animation<TextureRegion?>> = Hashtable()
        loadAnimationsFromConfig("scripts/player.json",animations)
        val idle =animations.get("IDLE")?.getKeyFrame(0F)
        engine.addEntity(Entity().apply {
            add(InputComponent())
            add(StateComponent())
            add(AnimationComponent(animations))
            add(TextureRegionComponent(TextureRegion(idle)))
            add(TransformComponent(Vector2(28F,5F),0F,1.5F))
            /*val body = world.createBody(BodyDef().apply {
                type = BodyDef.BodyType.DynamicBody
            })
            body.createFixture(PolygonShape().apply {
                setAsBox(idle?.regionWidth?.pixelToMeters!!/2F,idle?.regionHeight?.pixelToMeters!!/2F)
            },1.0F)
            body.setTransform(transform.position,0F)
            add(PhysicsComponent(body))*/
        })

        /*engine.addEntity(Entity().apply {
            add(TransformComponent(Vector2(0F,0F)))
            val body = world.createBody(BodyDef().apply {
                type = BodyDef.BodyType.StaticBody
            })
            body.createFixture(PolygonShape().apply {
                setAsBox(15F,1F)
            },1.0F)
            body.setTransform(transform.position,0F)
            add(PhysicsComponent(body))
        })*/
    }


    @Override
    override fun render() {
        Gdx.gl.glClearColor(1f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        engine.update(Gdx.graphics.deltaTime)
    }

    @Override
    override fun dispose() {
        batch.dispose()
        screen.dispose()

    }

    protected fun loadAnimation(textureName: String, points: Array<GridPoint2>, frameDuration: Float): Animation<TextureRegion?> {
        val tetureRegion = TextureRegion(Texture(textureName))
        val textureFrames = tetureRegion.split(16,16)
        val animationKeyFrames = arrayOfNulls<TextureRegion>(points.size)
        for (i in 0 until points.size) {
            animationKeyFrames[i] = textureFrames[points[i].x][points[i].y]
        }
        val animation = Animation(frameDuration, *animationKeyFrames)
        animation.playMode = Animation.PlayMode.LOOP
        return animation
    }
    protected  fun loadAnimationsFromConfig (configFilePath:String, animations : Hashtable<String, Animation<TextureRegion?>>){
        val entityConfig = Json().fromJson(EntityConfig::class.java, Gdx.files.internal(configFilePath))
        val animationConfigs = entityConfig.animationConfig
        for (animationConfig in animationConfigs) {
            val textureNames = animationConfig.texturePaths
            val points = animationConfig.gridPoints
            val animationType = animationConfig.animationType
            val frameDuration = animationConfig.frameDuration
            val animation  = loadAnimation(textureNames[0], points, frameDuration)

            animations[animationType] = animation
        }
    }
}


