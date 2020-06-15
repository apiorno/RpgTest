package com.mygdx.game.quests

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.mygdx.game.EntityConfig
import com.mygdx.game.maps.MapManager
import com.mygdx.game.profile.ProfileManager
import com.mygdx.game.quests.QuestTask.*
import com.mygdx.game.temporal.EntityFactory
import java.util.*

class QuestGraph {
    private var questTasks: Hashtable<String, QuestTask>? = null
    private var questTaskDependencies: Hashtable<String, ArrayList<QuestTaskDependency>>? = null
    var questTitle: String? = null
    var questID: String? = null
    var isQuestComplete = false
    var goldReward = 0
    var xpReward = 0

    fun areAllTasksComplete(): Boolean {
        val tasks = allQuestTasks
        for (task in tasks) {
            if (!task.isTaskComplete) {
                return false
            }
        }
        return true
    }

    fun setTasks(questTasks: Hashtable<String, QuestTask>) {
        require(questTasks.size >= 0) { "Can't have a negative amount of conversations" }
        this.questTasks = questTasks
        questTaskDependencies = Hashtable<String, ArrayList<QuestTaskDependency>>(questTasks.size)
        for (questTask in questTasks.values) {
            questTaskDependencies!![questTask.id] = ArrayList()
        }
    }

    val allQuestTasks: ArrayList<QuestTask>
        get() {
            val enumeration = questTasks!!.elements()
            return Collections.list(enumeration)
        }

    fun clear() {
        questTasks!!.clear()
        questTaskDependencies!!.clear()
    }

    private fun isValid(taskID: String?): Boolean {
        questTasks!![taskID] ?: return false
        return true
    }

    fun isReachable(sourceID: String?, sinkID: String?): Boolean {
        if (!isValid(sourceID) || !isValid(sinkID)) return false
        if (questTasks!![sourceID] == null) return false
        val list = questTaskDependencies!![sourceID] ?: return false
        for (dependency in list) {
            if (dependency.sourceId.equals(sourceID, ignoreCase = true) &&
                    dependency.destinationId.equals(sinkID, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun getQuestTaskByID(id: String?): QuestTask? {
        return if (!isValid(id)) {
            //System.out.println("Id " + id + " is not valid!");
            null
        } else questTasks!![id]
    }

    fun addDependency(questTaskDependency: QuestTaskDependency) {
        val list = questTaskDependencies!![questTaskDependency.sourceId] ?: return

        //Will not add if creates cycles
        if (doesCycleExist(questTaskDependency)) {
            //System.out.println("Cycle exists! Not adding");
            return
        }
        list.add(questTaskDependency)
    }

    fun doesCycleExist(questTaskDep: QuestTaskDependency): Boolean {
        val keys: Set<String?> = questTasks!!.keys
        for (id in keys) {
            if (doesQuestTaskHaveDependencies(id) &&
                    questTaskDep.destinationId.equals(id, ignoreCase = true)) {
                //System.out.println("ID: " + id + " destID: " + questTaskDep.getDestinationId());
                return true
            }
        }
        return false
    }

    fun doesQuestTaskHaveDependencies(id: String?): Boolean {
        getQuestTaskByID(id) ?: return false
        val list = questTaskDependencies!![id]!!
        return !(list.isEmpty() || list.size == 0)
    }

    fun updateQuestForReturn(): Boolean {
        val tasks = allQuestTasks
        var readyTask: QuestTask? = null

        //First, see if all tasks are available, meaning no blocking dependencies
        for (task in tasks) {
            if (!isQuestTaskAvailable(task.id)) {
                return false
            }
            if (!task.isTaskComplete) {
                readyTask = if (task.questType == QuestType.RETURN) {
                    task
                } else {
                    return false
                }
            }
        }
        if (readyTask == null) return false
        readyTask.setTaskComplete()
        return true
    }

    fun isQuestTaskAvailable(id: String?): Boolean {
        getQuestTaskByID(id) ?: return false
        val list = questTaskDependencies!![id]!!
        for (dep in list) {
            val depTask = getQuestTaskByID(dep.destinationId)
            if (depTask == null || depTask.isTaskComplete) continue
            if (dep.sourceId.equals(id, ignoreCase = true)) {
                return false
            }
        }
        return true
    }

    fun setQuestTaskComplete(id: String?) {
        val task = getQuestTaskByID(id) ?: return
        task.setTaskComplete()
    }

    fun update(mapMgr: MapManager) {
        val allQuestTasks = allQuestTasks
        loop@ for (questTask in allQuestTasks) {
            if (questTask.isTaskComplete) continue

            //We first want to make sure the task is available and is relevant to current location
            if (!isQuestTaskAvailable(questTask.id)) continue
            val taskLocation = questTask.getPropertyValue(QuestTaskPropertyType.TARGET_LOCATION.toString())
            if (taskLocation == null ||
                    taskLocation.isEmpty() ||
                    !taskLocation.equals(mapMgr.currentMapType.toString(), ignoreCase = true)) continue
            when (questTask.questType) {
                QuestType.FETCH -> {
                    val taskConfig = questTask.getPropertyValue(QuestTaskPropertyType.TARGET_TYPE.toString())
                    if (taskConfig == null || taskConfig.isEmpty()) break@loop
                    val config: EntityConfig = EntityConfig.getEntityConfig(taskConfig)
                    val questItemPositions = ProfileManager.instance.getProperty(config.entityID!!, Array::class.java)
                            ?: break@loop

                    //Case where all the items have been picked up
                    if (questItemPositions.size == 0) {
                        questTask.setTaskComplete()
                        Gdx.app.debug(TAG, "TASK : " + questTask.id + " is complete of Quest: " + questID)
                        Gdx.app.debug(TAG, "INFO : " + QuestTaskPropertyType.TARGET_TYPE.toString())
                    }
                }
                QuestType.KILL -> {
                }
                QuestType.DELIVERY -> {
                }
                QuestType.GUARD -> {
                }
                QuestType.ESCORT -> {
                }
                QuestType.RETURN -> {
                }
                QuestType.DISCOVER -> {
                }
            }
        }
    }

    fun init(mapMgr: MapManager) {
        val allQuestTasks = allQuestTasks
        loop@ for (questTask in allQuestTasks) {
            if (questTask.isTaskComplete) continue

            //We first want to make sure the task is available and is relevant to current location
            if (!isQuestTaskAvailable(questTask.id)) continue
            val taskLocation = questTask.getPropertyValue(QuestTaskPropertyType.TARGET_LOCATION.toString())
            if (taskLocation == null ||
                    taskLocation.isEmpty() ||
                    !taskLocation.equals(mapMgr.currentMapType.toString(), ignoreCase = true)) continue
            when (questTask.questType) {
                QuestType.FETCH -> {
                    val questEntities = Array<Entity>()
                    val positions = mapMgr.getQuestItemSpawnPositions(questID, questTask.id)
                    val taskConfig = questTask.getPropertyValue(QuestTaskPropertyType.TARGET_TYPE.toString())
                    if (taskConfig == null || taskConfig.isEmpty()) break@loop
                    val config: EntityConfig = EntityConfig.getEntityConfig(taskConfig)
                    var questItemPositions: Array<Vector2>? = ProfileManager.instance.getProperty(config.entityID!!, Array::class.java) as Array<Vector2>?
                    if (questItemPositions == null) questItemPositions = positions
                    for (questItemPosition in questItemPositions) {
                        // TODO:falta position
                        val entity = EntityFactory.instance!!.getEntity(EntityFactory.EntityType.NPC,config)
                        config.currentQuestID = questID
                        questEntities.add(entity)
                    }

                    //mapMgr.addMapQuestEntities(questEntities)
                    ProfileManager.instance.setProperty(config.entityID!!, questItemPositions)
                }
                QuestType.KILL -> {
                }
                QuestType.DELIVERY -> {
                }
                QuestType.GUARD -> {
                }
                QuestType.ESCORT -> {
                }
                QuestType.RETURN -> {
                }
                QuestType.DISCOVER -> {
                }
            }
        }
    }

    override fun toString(): String {
        return questTitle!!
    }

    fun toJson(): String {
        val json = Json()
        return json.prettyPrint(this)
    }

    companion object {
        private val TAG = QuestGraph::class.java.simpleName
    }
}