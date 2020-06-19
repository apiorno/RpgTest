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

    private var _json: Json? = null
    var entityConfig: EntityConfig? = null
    private var _components: Array<Component?>? = null
    private var _inputComponent: InputComponent? = null
    private var _graphicsComponent: GraphicsComponent? = null
    private var _physicsComponent: PhysicsComponent? = null

    constructor(entity: Entity) {
        set(entity)
    }

    private fun set(entity: Entity): Entity {
        _inputComponent = entity._inputComponent
        _graphicsComponent = entity._graphicsComponent
        _physicsComponent = entity._physicsComponent
        if (_components == null) {
            _components = Array(MAX_COMPONENTS)
        }
        _components!!.clear()
        _components!!.add(_inputComponent)
        _components!!.add(_physicsComponent)
        _components!!.add(_graphicsComponent)
        _json = entity._json
        entityConfig = EntityConfig(entity.entityConfig)
        return this
    }

    constructor(inputComponent: InputComponent?, physicsComponent: PhysicsComponent?, graphicsComponent: GraphicsComponent?) {
        entityConfig = EntityConfig()
        _json = Json()
        _components = Array(MAX_COMPONENTS)
        _inputComponent = inputComponent
        _physicsComponent = physicsComponent
        _graphicsComponent = graphicsComponent
        _components!!.add(_inputComponent)
        _components!!.add(_physicsComponent)
        _components!!.add(_graphicsComponent)
    }

    fun sendMessage(messageType: MESSAGE, vararg args: String) {
        var fullMessage = messageType.toString()
        for (string in args) {
            fullMessage += Component.Companion.MESSAGE_TOKEN + string
        }
        for (component in _components!!) {
            component!!.receiveMessage(fullMessage)
        }
    }

    fun registerObserver(observer: ComponentObserver?) {
        _inputComponent!!.addObserver(observer!!)
        _physicsComponent!!.addObserver(observer)
        _graphicsComponent!!.addObserver(observer)
    }

    fun unregisterObservers() {
        _inputComponent!!.removeAllObservers()
        _physicsComponent!!.removeAllObservers()
        _graphicsComponent!!.removeAllObservers()
    }

    fun update(mapMgr: MapManager, batch: Batch, delta: Float) {
        _inputComponent!!.update(this, delta)
        _physicsComponent!!.update(this, mapMgr, delta)
        _graphicsComponent!!.update(this, mapMgr, batch, delta)
    }

    fun updateInput(delta: Float) {
        _inputComponent!!.update(this, delta)
    }

    fun dispose() {
        for (component in _components!!) {
            component!!.dispose()
        }
    }

    val currentBoundingBox: Rectangle?
        get() = _physicsComponent!!._boundingBox

    val currentPosition: Vector2?
        get() = _graphicsComponent!!._currentPosition

    val inputProcessor: InputProcessor?
        get() = _inputComponent

    fun getAnimation(type: AnimationType): Animation<TextureRegion>? {
        return _graphicsComponent!!.getAnimation(type)
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
            val serializedConfig = ProfileManager.instance.getProperty(entityConfig.entityID!!, EntityConfig::class.java)
            return serializedConfig ?: entityConfig
        }

        fun loadEntityConfig(entityConfig: EntityConfig): EntityConfig {
            val serializedConfig = ProfileManager.instance.getProperty(entityConfig.entityID!!, EntityConfig::class.java)
            return serializedConfig ?: entityConfig
        }

        fun initEntity(entityConfig: EntityConfig?, position: Vector2?): Entity? {
            val json = Json()
            val entity: Entity = EntityFactory.getEntity(EntityType.NPC)!!
            entity.entityConfig = entityConfig
            entity.sendMessage(MESSAGE.LOAD_ANIMATIONS, json.toJson(entity.entityConfig))
            entity.sendMessage(MESSAGE.INIT_START_POSITION, json.toJson(position))
            entity.sendMessage(MESSAGE.INIT_STATE, json.toJson(entity.entityConfig?.state))
            entity.sendMessage(MESSAGE.INIT_DIRECTION, json.toJson(entity.entityConfig?.direction))
            return entity
        }

        @kotlin.jvm.JvmStatic
        fun initEntities(configs: Array<EntityConfig>): Hashtable<String?, Entity?> {
            val json = Json()
            val entities = Hashtable<String?, Entity?>()
            for (config in configs) {
                val entity: Entity = EntityFactory.getEntity(EntityType.NPC)!!
                entity.entityConfig = config
                entity.sendMessage(MESSAGE.LOAD_ANIMATIONS, json.toJson(entity.entityConfig))
                entity.sendMessage(MESSAGE.INIT_START_POSITION, json.toJson(Vector2(0F, 0F)))
                entity.sendMessage(MESSAGE.INIT_STATE, json.toJson(entity.entityConfig?.state))
                entity.sendMessage(MESSAGE.INIT_DIRECTION, json.toJson(entity.entityConfig?.direction))
                entities[entity.entityConfig!!.entityID] = entity
            }
            return entities
        }

        fun initEntity(entityConfig: EntityConfig?): Entity? {
            val json = Json()
            val entity: Entity = EntityFactory.getEntity(EntityType.NPC)!!
            entity.entityConfig = entityConfig
            entity.sendMessage(MESSAGE.LOAD_ANIMATIONS, json.toJson(entity.entityConfig))
            entity.sendMessage(MESSAGE.INIT_START_POSITION, json.toJson(Vector2(0F, 0F)))
            entity.sendMessage(MESSAGE.INIT_STATE, json.toJson(entity.entityConfig?.state))
            entity.sendMessage(MESSAGE.INIT_DIRECTION, json.toJson(entity.entityConfig?.direction))
            return entity
        }
    }
}