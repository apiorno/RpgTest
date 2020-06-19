package com.mygdx.game.ecs

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.mygdx.game.ecs.Component.MESSAGE
import java.util.*

class EntityFactory private constructor() {
    private val _entities: Hashtable<String?, EntityConfig>

    enum class EntityType {
        PLAYER, PLAYER_DEMO, NPC
    }

    enum class EntityName {
        PLAYER_PUPPET, TOWN_GUARD_WALKING, TOWN_BLACKSMITH, TOWN_MAGE, TOWN_INNKEEPER, TOWN_FOLK1, TOWN_FOLK2, TOWN_FOLK3, TOWN_FOLK4, TOWN_FOLK5, TOWN_FOLK6, TOWN_FOLK7, TOWN_FOLK8, TOWN_FOLK9, TOWN_FOLK10, TOWN_FOLK11, TOWN_FOLK12, TOWN_FOLK13, TOWN_FOLK14, TOWN_FOLK15, FIRE
    }

    fun getEntityByName(entityName: EntityName): Entity? {
        val config = EntityConfig(_entities[entityName.toString()])
        return Entity.Companion.initEntity(config)
    }

    companion object {
        private val TAG = EntityFactory::class.java.simpleName
        private val _json = Json()
        private var _instance: EntityFactory? = null
        var PLAYER_CONFIG = "scripts/player.json"
        var TOWN_GUARD_WALKING_CONFIG = "scripts/town_guard_walking.json"
        var TOWN_BLACKSMITH_CONFIG = "scripts/town_blacksmith.json"
        var TOWN_MAGE_CONFIG = "scripts/town_mage.json"
        var TOWN_INNKEEPER_CONFIG = "scripts/town_innkeeper.json"
        var TOWN_FOLK_CONFIGS = "scripts/town_folk.json"
        var ENVIRONMENTAL_ENTITY_CONFIGS = "scripts/environmental_entities.json"
        @kotlin.jvm.JvmStatic
        val instance: EntityFactory?
            get() {
                if (_instance == null) {
                    _instance = EntityFactory()
                }
                return _instance
            }

        @kotlin.jvm.JvmStatic
        fun getEntity(entityType: EntityType?): Entity? {
            var entity: Entity?
            return when (entityType) {
                EntityType.PLAYER -> {
                    entity = Entity(PlayerInputComponent(), PlayerPhysicsComponent(), PlayerGraphicsComponent())
                    entity.entityConfig = Entity.Companion.getEntityConfig(PLAYER_CONFIG)
                    entity.sendMessage(MESSAGE.LOAD_ANIMATIONS, _json.toJson(entity.entityConfig))
                    entity
                }
                EntityType.PLAYER_DEMO -> {
                    entity = Entity(NPCInputComponent(), PlayerPhysicsComponent(), PlayerGraphicsComponent())
                    entity
                }
                EntityType.NPC -> {
                    entity = Entity(NPCInputComponent(), NPCPhysicsComponent(), NPCGraphicsComponent())
                    entity
                }
                else -> null
            }
        }
    }

    init {
        _entities = Hashtable()
        val townFolkConfigs: Array<EntityConfig> = Entity.Companion.getEntityConfigs(TOWN_FOLK_CONFIGS)
        for (config in townFolkConfigs) {
            _entities[config.entityID] = config
        }
        val environmentalEntityConfigs: Array<EntityConfig> = Entity.Companion.getEntityConfigs(ENVIRONMENTAL_ENTITY_CONFIGS)
        for (config in environmentalEntityConfigs) {
            _entities[config.entityID] = config
        }
        _entities[EntityName.TOWN_GUARD_WALKING.toString()] = Entity.Companion.loadEntityConfigByPath(TOWN_GUARD_WALKING_CONFIG)
        _entities[EntityName.TOWN_BLACKSMITH.toString()] = Entity.Companion.loadEntityConfigByPath(TOWN_BLACKSMITH_CONFIG)
        _entities[EntityName.TOWN_MAGE.toString()] = Entity.Companion.loadEntityConfigByPath(TOWN_MAGE_CONFIG)
        _entities[EntityName.TOWN_INNKEEPER.toString()] = Entity.Companion.loadEntityConfigByPath(TOWN_INNKEEPER_CONFIG)
        _entities[EntityName.PLAYER_PUPPET.toString()] = Entity.Companion.loadEntityConfigByPath(PLAYER_CONFIG)
    }
}