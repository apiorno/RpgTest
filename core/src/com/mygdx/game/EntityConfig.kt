package com.mygdx.game

import AnimationType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.GridPoint2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue
import com.badlogic.gdx.utils.ObjectMap
import com.mygdx.game.widgets.InventoryItem.*
import java.util.ArrayList

class EntityConfig {
    var animationConfig: Array<AnimationConfig>
        private set
    var inventory: Array<ItemTypeID>
    var state = State.IDLE
    var direction = Direction.DOWN
    var entityID: String? = null
    var conversationConfigPath: String? = null
    var questConfigPath: String? = null
    var currentQuestID: String? = null
    var itemTypeID: String? = null
    var entityProperties: ObjectMap<String, String>

    enum class EntityProperties {
        ENTITY_HEALTH_POINTS, ENTITY_ATTACK_POINTS, ENTITY_DEFENSE_POINTS, ENTITY_HIT_DAMAGE_TOTAL, ENTITY_XP_REWARD, ENTITY_GP_REWARD, NONE
    }

    internal constructor() {
        animationConfig = Array()
        inventory = Array()
        entityProperties = ObjectMap()
    }

    internal constructor(config: EntityConfig?) {
        state = config!!.state
        direction = config.direction
        entityID = config.entityID
        conversationConfigPath = config.conversationConfigPath
        questConfigPath = config.questConfigPath
        currentQuestID = config.currentQuestID
        itemTypeID = config.itemTypeID
        animationConfig = Array()
        animationConfig.addAll(config.animationConfig)
        inventory = Array()
        inventory.addAll(config.inventory)
        entityProperties = ObjectMap()
        entityProperties.putAll(config.entityProperties)
    }

    fun setPropertyValue(key: String, value: String) {
        entityProperties.put(key, value)
    }

    fun getPropertyValue(key: String): String {
        return entityProperties[key] ?: return String()
    }

    fun addAnimationConfig(animationConfig: AnimationConfig) {
        this.animationConfig.add(animationConfig)
    }

    class AnimationConfig {
        var frameDuration = 1.0f
        var animationType: AnimationType = AnimationType.IDLE
        var texturePaths: Array<String> = Array()
        var gridPoints: Array<GridPoint2> = Array()

    }
    companion object{

        @kotlin.jvm.JvmStatic
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
    }
}