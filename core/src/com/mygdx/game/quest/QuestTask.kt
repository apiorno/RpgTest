package com.mygdx.game.quest

import com.badlogic.gdx.utils.ObjectMap

class QuestTask {
    enum class QuestType {
        FETCH, KILL, DELIVERY, GUARD, ESCORT, RETURN, DISCOVER
    }

    enum class QuestTaskPropertyType {
        IS_TASK_COMPLETE, TARGET_TYPE, TARGET_NUM, TARGET_LOCATION, NONE
    }

    private var taskProperties: ObjectMap<String, String> = ObjectMap()
    lateinit var id: String
    lateinit var taskPhrase: String
    lateinit var questType: QuestType

    val isTaskComplete: Boolean
        get() {
            if (!taskProperties.containsKey(QuestTaskPropertyType.IS_TASK_COMPLETE.toString())) {
                setPropertyValue(QuestTaskPropertyType.IS_TASK_COMPLETE.toString(), "false")
                return false
            }
            val value = taskProperties[QuestTaskPropertyType.IS_TASK_COMPLETE.toString()].toString()
            return java.lang.Boolean.parseBoolean(value)
        }

    fun setTaskComplete() {
        setPropertyValue(QuestTaskPropertyType.IS_TASK_COMPLETE.toString(), "true")
    }

    fun resetAllProperties() {
        taskProperties.put(QuestTaskPropertyType.IS_TASK_COMPLETE.toString(), "false")
    }

    private fun setPropertyValue(key: String, value: String) {
        taskProperties.put(key, value)
    }

    fun getPropertyValue(key: String): String {
        return taskProperties[key] ?: return String()
    }

    override fun toString(): String {
        return taskPhrase
    }

}