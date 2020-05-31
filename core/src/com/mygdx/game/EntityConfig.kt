package com.mygdx.game

import AnimationType
import com.badlogic.gdx.math.GridPoint2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap

class EntityConfig {
    var animationConfig: Array<AnimationConfig>
        private set
    //var inventory: Array<ItemTypeID>
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
        //inventory = Array()
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
        /*inventory = Array()
        inventory.addAll(config.inventory)*/
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
}