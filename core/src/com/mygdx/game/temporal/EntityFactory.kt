package com.mygdx.game.temporal

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.mygdx.game.EntityConfig
import getEntityConfigs
import initEntity
import loadEntityConfigByPath
import java.util.*

class EntityFactory private constructor() {
    private val entities: Hashtable<String?, EntityConfig> = Hashtable()

    enum class EntityType {
        PLAYER, PLAYER_DEMO, NPC
    }

    enum class EntityName {
        PLAYER_PUPPET, TOWN_GUARD_WALKING, TOWN_BLACKSMITH, TOWN_MAGE, TOWN_INNKEEPER, TOWN_FOLK1, TOWN_FOLK2, TOWN_FOLK3, TOWN_FOLK4, TOWN_FOLK5, TOWN_FOLK6, TOWN_FOLK7, TOWN_FOLK8, TOWN_FOLK9, TOWN_FOLK10, TOWN_FOLK11, TOWN_FOLK12, TOWN_FOLK13, TOWN_FOLK14, TOWN_FOLK15, FIRE
    }

    fun getEntityByName(entityName: EntityName): Entity? {
        val config = EntityConfig(entities[entityName.toString()])
        return initEntity(config)
    }

    companion object {
        private val TAG = EntityFactory::class.java.simpleName
        private val json = Json()
        private var uniqueInstance: EntityFactory? = null
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
                if (uniqueInstance == null) {
                    uniqueInstance = EntityFactory()
                }
                return uniqueInstance
            }

        @kotlin.jvm.JvmStatic
        fun getEntity(entityType: EntityType?): Entity? {
            var entity: Entity? = null
            return when (entityType) {
                EntityType.PLAYER -> {

                    /*entity = Entity(PlayerInputComponent(), PlayerPhysicsComponent(), PlayerGraphicsComponent())
                    entity.entityConfig = Entity.Companion.getEntityConfig(PLAYER_CONFIG)
                    entity.sendMessage(MESSAGE.LOAD_ANIMATIONS, _json.toJson(entity.entityConfig))*/
                    entity
                }
                EntityType.PLAYER_DEMO -> {
                    /*entity = Entity(NPCInputComponent(), PlayerPhysicsComponent(), PlayerGraphicsComponent())*/
                    entity
                }
                EntityType.NPC -> {
                    /*entity = Entity(NPCInputComponent(), NPCPhysicsComponent(), NPCGraphicsComponent())*/
                    entity
                }
                else -> null
            }
        }
    }

    init {
        val townFolkConfigs: Array<EntityConfig> = getEntityConfigs(TOWN_FOLK_CONFIGS)
        for (config in townFolkConfigs) {
            entities[config.entityID] = config
        }
        val environmentalEntityConfigs: Array<EntityConfig> = getEntityConfigs(ENVIRONMENTAL_ENTITY_CONFIGS)
        for (config in environmentalEntityConfigs) {
            entities[config.entityID] = config
        }
        entities[EntityName.TOWN_GUARD_WALKING.toString()] = loadEntityConfigByPath(TOWN_GUARD_WALKING_CONFIG)
        entities[EntityName.TOWN_BLACKSMITH.toString()] = loadEntityConfigByPath(TOWN_BLACKSMITH_CONFIG)
        entities[EntityName.TOWN_MAGE.toString()] = loadEntityConfigByPath(TOWN_MAGE_CONFIG)
        entities[EntityName.TOWN_INNKEEPER.toString()] = loadEntityConfigByPath(TOWN_INNKEEPER_CONFIG)
        entities[EntityName.PLAYER_PUPPET.toString()] = loadEntityConfigByPath(PLAYER_CONFIG)
    }
}