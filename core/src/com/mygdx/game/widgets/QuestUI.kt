package com.mygdx.game.widgets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.mygdx.game.Utility
import com.mygdx.game.maps.MapManager
import com.mygdx.game.profile.ProfileManager
import com.mygdx.game.quests.QuestGraph
import com.mygdx.game.quests.QuestTask

class QuestUI : Window("Quest Log", Utility.STATUSUI_SKIN, "solidbackground") {
    private val listQuests = List<QuestGraph>(Utility.STATUSUI_SKIN)
    private val listTasks: List<QuestTask>

    private val json = Json()
    var quests: Array<QuestGraph> = Array()
        set(quests) {
            this@QuestUI.quests = quests
            updateQuestItemList()
            field = quests
        }
    private val questLabel: Label = Label("Quests:", Utility.STATUSUI_SKIN)
    private val tasksLabel: Label = Label("Tasks:", Utility.STATUSUI_SKIN)
    fun questTaskComplete(questID: String?, questTaskID: String?) {
        for (questGraph in quests) {
            if (questGraph.questID.equals(questID, ignoreCase = true)) {
                if (questGraph.isQuestTaskAvailable(questTaskID)) {
                    questGraph.setQuestTaskComplete(questTaskID)
                } else {
                    return
                }
            }
        }
    }

    fun loadQuest(questConfigPath: String?): QuestGraph? {
        if (questConfigPath!!.isEmpty() || !Gdx.files.internal(questConfigPath).exists()) {
            Gdx.app.debug(TAG, "Quest file does not exist!")
            return null
        }
        val graph = json.fromJson(QuestGraph::class.java, Gdx.files.internal(questConfigPath))
        if (doesQuestExist(graph.questID)) {
            return null
        }
        clearDialog()
        quests.add(graph)
        updateQuestItemList()
        return graph
    }

    fun isQuestReadyForReturn(questID: String?): Boolean {
        if (questID!!.isEmpty()) {
            Gdx.app.debug(TAG, "Quest ID not valid")
            return false
        }
        if (!doesQuestExist(questID)) return false
        val graph = getQuestByID(questID) ?: return false
        if (graph.updateQuestForReturn()) {
            graph.isQuestComplete = true
        } else {
            return false
        }
        return true
    }

    fun getQuestByID(questGraphID: String?): QuestGraph? {
        for (questGraph in quests) {
            if (questGraph.questID.equals(questGraphID, ignoreCase = true)) {
                return questGraph
            }
        }
        return null
    }

    private fun doesQuestExist(questGraphID: String?): Boolean {
        for (questGraph in quests) {
            if (questGraph.questID.equals(questGraphID, ignoreCase = true)) {
                return true
            }
        }
        return false
    }



    private fun updateQuestItemList() {
        clearDialog()
        listQuests.setItems(quests)
        listQuests.selectedIndex = -1
    }

    private fun clearDialog() {
        listQuests.clearItems()
        listTasks.clearItems()
    }

    private fun populateQuestTaskDialog(graph: QuestGraph) {
        listTasks.clearItems()
        val tasks = graph.allQuestTasks
        listTasks.setItems(*tasks.toTypedArray())
        listTasks.selectedIndex = -1
    }

    fun initQuests(mapMgr: MapManager) {
        //mapMgr.clearAllMapQuestEntities()

        //populate items if quests have them
        for (quest in quests) {
            if (!quest.isQuestComplete) {
                quest.init(mapMgr)
            }
        }
        ProfileManager.instance.setProperty("playerQuests", quests)
    }

    fun updateQuests(mapMgr: MapManager?) {
        for (quest in quests) {
            if (!quest.isQuestComplete) {
                quest.update(mapMgr!!)
            }
        }
        ProfileManager.instance.setProperty("playerQuests", quests)
    }

    companion object {
        private val TAG = QuestUI::class.java.simpleName
        const val RETURN_QUEST = "conversations/return_quest.json"
        const val FINISHED_QUEST = "conversations/quest_finished.json"
    }

    init {
        //create
        val scrollPane = ScrollPane(listQuests, Utility.STATUSUI_SKIN, "inventoryPane")
        scrollPane.setOverscroll(false, false)
        scrollPane.fadeScrollBars = false
        scrollPane.setForceScroll(true, false)
        listTasks = List<QuestTask>(Utility.STATUSUI_SKIN)
        val scrollPaneTasks = ScrollPane(listTasks, Utility.STATUSUI_SKIN, "inventoryPane")
        scrollPaneTasks.setOverscroll(false, false)
        scrollPaneTasks.fadeScrollBars = false
        scrollPaneTasks.setForceScroll(true, false)

        //layout
        this.add(questLabel).align(Align.left)
        this.add(tasksLabel).align(Align.left)
        row()
        defaults().expand().fill()
        this.add(scrollPane).padRight(15f)
        this.add(scrollPaneTasks).padLeft(5f)

        //this.debug();
        pack()

        //Listeners
        listQuests.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                val quest = listQuests.selected as QuestGraph
                populateQuestTaskDialog(quest)
            }
        }
        )
    }
}