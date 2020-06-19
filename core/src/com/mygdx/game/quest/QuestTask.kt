package com.mygdx.game.quest

import com.badlogic.gdx.utils.ObjectMap

class QuestTask {
    enum class QuestType {
        FETCH, KILL, DELIVERY, GUARD, ESCORT, RETURN, DISCOVER
    }

    enum class QuestTaskPropertyType {
        IS_TASK_COMPLETE, TARGET_TYPE, TARGET_NUM, TARGET_LOCATION, NONE
    }

    var taskProperties: ObjectMap<String, Any>
    var id: String? = null
    var taskPhrase: String? = null
    var questType: QuestType? = null

    val isTaskComplete: Boolean
        get() {
            if (!taskProperties.containsKey(QuestTaskPropertyType.IS_TASK_COMPLETE.toString())) {
                setPropertyValue(QuestTaskPropertyType.IS_TASK_COMPLETE.toString(), "false")
                return false
            }
            val `val` = taskProperties[QuestTaskPropertyType.IS_TASK_COMPLETE.toString()].toString()
            return java.lang.Boolean.parseBoolean(`val`)
        }

    fun setTaskComplete() {
        setPropertyValue(QuestTaskPropertyType.IS_TASK_COMPLETE.toString(), "true")
    }

    fun resetAllProperties() {
        taskProperties.put(QuestTaskPropertyType.IS_TASK_COMPLETE.toString(), "false")
    }

    fun setPropertyValue(key: String, value: String) {
        taskProperties.put(key, value)
    }

    fun getPropertyValue(key: String): String {
        val propertyVal = taskProperties[key] ?: return String()
        return propertyVal.toString()
    }

    override fun toString(): String {
        return taskPhrase!!
    }

    init {
        taskProperties = ObjectMap()
    }
}