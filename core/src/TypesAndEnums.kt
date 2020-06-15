import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue
import com.mygdx.game.EntityConfig
import com.mygdx.game.profile.ProfileManager
import java.util.*

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
enum class MESSAGE {
    CURRENT_POSITION, INIT_START_POSITION, CURRENT_DIRECTION, CURRENT_STATE, COLLISION_WITH_MAP, COLLISION_WITH_ENTITY, LOAD_ANIMATIONS, INIT_DIRECTION, INIT_STATE, INIT_SELECT_ENTITY, ENTITY_SELECTED, ENTITY_DESELECTED
}

const val MESSAGE_TOKEN = ":::::"

const val UNIT_SCALE = 1 / 16f

    const val FRAME_WIDTH = 16
    const val FRAME_HEIGHT = 16

    fun getEntityConfig(configFilePath: String?): EntityConfig {
        val json = Json()
        return json.fromJson(EntityConfig::class.java, Gdx.files.internal(configFilePath))
    }


    @Suppress("UNCHECKED_CAST")
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




