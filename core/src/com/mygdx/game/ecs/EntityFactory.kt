package com.mygdx.game.ecs

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.mygdx.game.ecs.Component.MESSAGE
import java.util.*

object EntityFactory{
    private val TAG = EntityFactory::class.java.simpleName
    private val json = Json()
    private const val PLAYER_CONFIG = "scripts/player.json"
    private const val TOWN_GUARD_WALKING_CONFIG = "scripts/town_guard_walking.json"
    private const val TOWN_BLACKSMITH_CONFIG = "scripts/town_blacksmith.json"
    private const val TOWN_MAGE_CONFIG = "scripts/town_mage.json"
    private const val TOWN_INNKEEPER_CONFIG = "scripts/town_innkeeper.json"
    private const val TOWN_FOLK_CONFIGS = "scripts/town_folk.json"
    private const val ENVIRONMENTAL_ENTITY_CONFIGS = "scripts/environmental_entities.json"
    private val entities: Hashtable<String?, EntityConfig> = Hashtable()

    enum class EntityType {
        PLAYER, PLAYER_DEMO, NPC
    }

    enum class EntityName {
        PLAYER_PUPPET, TOWN_GUARD_WALKING, TOWN_BLACKSMITH, TOWN_MAGE, TOWN_INNKEEPER, TOWN_FOLK1, TOWN_FOLK2, TOWN_FOLK3, TOWN_FOLK4, TOWN_FOLK5, TOWN_FOLK6, TOWN_FOLK7, TOWN_FOLK8, TOWN_FOLK9, TOWN_FOLK10, TOWN_FOLK11, TOWN_FOLK12, TOWN_FOLK13, TOWN_FOLK14, TOWN_FOLK15, FIRE
    }

    fun getEntityByName(entityName: EntityName): Entity? {
        val config = entities[entityName.toString()]?.let { EntityConfig(it) }
        return config?.let { Entity.initEntity(it) }
    }

    fun getEntity(entityType: EntityType?): Entity? {
        val entity: Entity?
        return when (entityType) {
            EntityType.PLAYER -> {
                entity = Entity(PlayerInputComponent(), PlayerPhysicsComponent(), PlayerGraphicsComponent())
                entity.entityConfig = Entity.getEntityConfig(PLAYER_CONFIG)
                entity.sendMessage(MESSAGE.LOAD_ANIMATIONS, json.toJson(entity.entityConfig))
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

    init {
        val townFolkConfigs: Array<EntityConfig> = Entity.getEntityConfigs(TOWN_FOLK_CONFIGS)
        townFolkConfigs.forEach { entities[it.entityID] = it }
        val environmentalEntityConfigs: Array<EntityConfig> = Entity.getEntityConfigs(ENVIRONMENTAL_ENTITY_CONFIGS)
        environmentalEntityConfigs.forEach { entities[it.entityID] = it }
        entities[EntityName.TOWN_GUARD_WALKING.toString()] = Entity.loadEntityConfigByPath(TOWN_GUARD_WALKING_CONFIG)
        entities[EntityName.TOWN_BLACKSMITH.toString()] = Entity.loadEntityConfigByPath(TOWN_BLACKSMITH_CONFIG)
        entities[EntityName.TOWN_MAGE.toString()] = Entity.loadEntityConfigByPath(TOWN_MAGE_CONFIG)
        entities[EntityName.TOWN_INNKEEPER.toString()] = Entity.loadEntityConfigByPath(TOWN_INNKEEPER_CONFIG)
        entities[EntityName.PLAYER_PUPPET.toString()] = Entity.loadEntityConfigByPath(PLAYER_CONFIG)
    }
}