package com.mygdx.game.ecs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue
import com.mygdx.game.ecs.Component.MESSAGE
import com.mygdx.game.ecs.EntityFactory.EntityType
import com.mygdx.game.maps.MapManager
import com.mygdx.game.profile.ProfileManager
import java.util.*

class Entity {
    enum class Direction {
        UP, RIGHT, DOWN, LEFT;

        val opposite: Direction
            get() = if (this == LEFT) {
                RIGHT
            } else if (this == RIGHT) {
                LEFT
            } else if (this == UP) {
                DOWN
            } else {
                UP
            }

        companion object {
            val randomNext: Direction
                get() = values()[MathUtils.random(values().size - 1)]
        }
    }

    enum class State {
        IDLE, WALKING, IMMOBILE;

        companion object {
            //Ignore IMMOBILE which should be last state
            //This should always be last
            val randomNext: State
                get() =//Ignore IMMOBILE which should be last state
                    values()[MathUtils.random(values().size - 2)]
        }
    }

    enum class AnimationType {
        WALK_LEFT, WALK_RIGHT, WALK_UP, WALK_DOWN, IDLE, IMMOBILE
    }

    private var json: Json = Json()
    var entityConfig: EntityConfig = EntityConfig()
    private var components: Array<Component> = Array(MAX_COMPONENTS)
    private var inputComponent: InputComponent? = null
    private var graphicsComponent: GraphicsComponent? = null
    private var physicsComponent: PhysicsComponent? = null

    constructor(entity: Entity) {
        set(entity)
    }

    private fun set(entity: Entity): Entity {
        inputComponent = entity.inputComponent
        graphicsComponent = entity.graphicsComponent
        physicsComponent = entity.physicsComponent
        components.clear()
        components.add(inputComponent)
        components.add(physicsComponent)
        components.add(graphicsComponent)
        json = entity.json
        entityConfig = EntityConfig(entity.entityConfig)
        return this
    }

    constructor(inputComponent: InputComponent?, physicsComponent: PhysicsComponent?, graphicsComponent: GraphicsComponent?) {
        this.inputComponent = inputComponent
        this.physicsComponent = physicsComponent
        this.graphicsComponent = graphicsComponent
        components.add(this.inputComponent)
        components.add(this.physicsComponent)
        components.add(this.graphicsComponent)
    }

    fun sendMessage(messageType: MESSAGE, vararg args: String) {
        var fullMessage = messageType.toString()
        args.forEach { fullMessage += Component.MESSAGE_TOKEN + it }
        components.forEach { it.receiveMessage(fullMessage) }
    }

    fun registerObserver(observer: ComponentObserver?) {
        inputComponent!!.addObserver(observer!!)
        physicsComponent!!.addObserver(observer)
        graphicsComponent!!.addObserver(observer)
    }

    fun unregisterObservers() {
        inputComponent!!.removeAllObservers()
        physicsComponent!!.removeAllObservers()
        graphicsComponent!!.removeAllObservers()
    }

    fun update(mapMgr: MapManager, batch: Batch, delta: Float) {
        inputComponent!!.update(this, delta)
        physicsComponent!!.update(this, mapMgr, delta)
        graphicsComponent!!.update(this, mapMgr, batch, delta)
    }

    fun updateInput(delta: Float) {
        inputComponent!!.update(this, delta)
    }

    fun dispose() {
        components.forEach { it.dispose() }

    }

    val currentBoundingBox: Rectangle?
        get() = physicsComponent!!.boundingBox

    val currentPosition: Vector2?
        get() = graphicsComponent!!.currentPosition

    val inputProcessor: InputProcessor?
        get() = inputComponent

    fun getAnimation(type: AnimationType): Animation<TextureRegion>? {
        return graphicsComponent!!.getAnimation(type)
    }

    companion object {
        private val TAG = Entity::class.java.simpleName
        const val FRAME_WIDTH = 16
        const val FRAME_HEIGHT = 16
        private const val MAX_COMPONENTS = 5
        fun getEntityConfig(configFilePath: String?): EntityConfig {
            val json = Json()
            return json.fromJson(EntityConfig::class.java, Gdx.files.internal(configFilePath))
        }

        @Suppress("UNCHECKED_CAST")
        @kotlin.jvm.JvmStatic
        fun getEntityConfigs(configFilePath: String?): Array<EntityConfig> {
            val json = Json()
            val configs = Array<EntityConfig>()
            val list: ArrayList<JsonValue> = json.fromJson(ArrayList::class.java, Gdx.files.internal(configFilePath)) as ArrayList<JsonValue>
            for (jsonVal in list) {
                configs.add(json.readValue(EntityConfig::class.java, jsonVal))
            }
            return configs
        }

        fun loadEntityConfigByPath(entityConfigPath: String?): EntityConfig {
            val entityConfig = getEntityConfig(entityConfigPath)
            val serializedConfig = ProfileManager.getProperty(entityConfig.entityID!!, EntityConfig::class.java)
            return serializedConfig ?: entityConfig
        }

        fun loadEntityConfig(entityConfig: EntityConfig): EntityConfig {
            val serializedConfig = ProfileManager.getProperty(entityConfig.entityID!!, EntityConfig::class.java)
            return serializedConfig ?: entityConfig
        }

        fun initEntity(entityConfig: EntityConfig, position: Vector2?): Entity? {
            val json = Json()
            val entity: Entity = EntityFactory.getEntity(EntityType.NPC)!!
            entity.entityConfig = entityConfig
            entity.sendMessage(MESSAGE.LOAD_ANIMATIONS, json.toJson(entity.entityConfig))
            entity.sendMessage(MESSAGE.INIT_START_POSITION, json.toJson(position))
            entity.sendMessage(MESSAGE.INIT_STATE, json.toJson(entity.entityConfig.state))
            entity.sendMessage(MESSAGE.INIT_DIRECTION, json.toJson(entity.entityConfig.direction))
            return entity
        }

        @kotlin.jvm.JvmStatic
        fun initEntities(configs: Array<EntityConfig>): Hashtable<String?, Entity?> {
            val json = Json()
            val entities = Hashtable<String?, Entity?>()
            configs.forEach {
                val entity: Entity = EntityFactory.getEntity(EntityType.NPC)!!
                entity.entityConfig = it
                entity.sendMessage(MESSAGE.LOAD_ANIMATIONS, json.toJson(entity.entityConfig))
                entity.sendMessage(MESSAGE.INIT_START_POSITION, json.toJson(Vector2(0F, 0F)))
                entity.sendMessage(MESSAGE.INIT_STATE, json.toJson(entity.entityConfig.state))
                entity.sendMessage(MESSAGE.INIT_DIRECTION, json.toJson(entity.entityConfig.direction))
                entities[entity.entityConfig.entityID] = entity
            }

            return entities
        }

        fun initEntity(entityConfig: EntityConfig): Entity? {
            val json = Json()
            val entity: Entity = EntityFactory.getEntity(EntityType.NPC)!!
            entity.entityConfig = entityConfig
            entity.sendMessage(MESSAGE.LOAD_ANIMATIONS, json.toJson(entity.entityConfig))
            entity.sendMessage(MESSAGE.INIT_START_POSITION, json.toJson(Vector2(0F, 0F)))
            entity.sendMessage(MESSAGE.INIT_STATE, json.toJson(entity.entityConfig.state))
            entity.sendMessage(MESSAGE.INIT_DIRECTION, json.toJson(entity.entityConfig.direction))
            return entity
        }
    }
}