package com.mygdx.game.temporal

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.mygdx.game.*
import getEntityConfigs
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
    fun getPlayer () : Entity?{
        return getEntity(EntityType.PLAYER,getEntityConfigByName(EntityName.PLAYER_PUPPET))
    }
    fun getEntityByName(entityName: EntityName): Entity? {
        val config = getEntityConfigByName(entityName)
        return getEntity(EntityType.NPC,config)
    }

     fun getEntityConfigByName(entityName: EntityName): EntityConfig {
        return EntityConfig(entities[entityName.toString()])
    }

    fun getEntity(entityType: EntityType?, entityConfig: EntityConfig): Entity? {
        var entity: Entity? = null
        return when (entityType) {
            EntityType.PLAYER -> {
                val animationManager = AnimationManager(entityConfig)

                val idle = animationManager.getAnimation(AnimationType.IDLE).getKeyFrame(0F)
                entity =Entity().apply {
                    add(InputComponent())
                    add(StateComponent())
                    add(AnimationComponent(animationManager.animations))
                    add(TextureRegionComponent(TextureRegion(idle)))
                    add(TransformComponent(Vector2(28F, 5F), 0F, 1.5F))
                }
                entity
            }
            EntityType.PLAYER_DEMO -> {
                /*entity = Entity(NPCInputComponent(), PlayerPhysicsComponent(), PlayerGraphicsComponent())*/
                entity
            }
            EntityType.NPC -> {
                /*entity = Entity(NPCInputComponent(), NPCPhysicsComponent(), NPCGraphicsComponent())*/
                /*
                entity.sendMessage(MESSAGE.LOAD_ANIMATIONS, json.toJson(entity.entityConfig))
            entity.sendMessage(MESSAGE.INIT_START_POSITION, json.toJson(Vector2(0f, 0f)))
            entity.sendMessage(MESSAGE.INIT_STATE, json.toJson(entity.entityConfig!!.state))
            entity.sendMessage(MESSAGE.INIT_DIRECTION, json.toJson(entity.entityConfig!!.direction))
                 */
                entity
            }
            else -> null
        }
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