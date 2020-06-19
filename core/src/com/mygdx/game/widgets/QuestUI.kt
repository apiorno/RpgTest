package com.mygdx.game.widgets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.mygdx.game.maps.MapManager
import com.mygdx.game.Utility
import com.mygdx.game.profile.ProfileManager
import com.mygdx.game.quest.QuestGraph
import com.mygdx.game.quest.QuestTask

class QuestUI : Window("Quest Log", Utility.STATUSUI_SKIN, "solidbackground") {
    private val _listQuests: List<QuestGraph>
    private val _listTasks: List<QuestTask>
    private val _json: Json
    private var _quests: Array<QuestGraph>
    private val _questLabel: Label
    private val _tasksLabel: Label
    fun questTaskComplete(questID: String?, questTaskID: String?) {
        for (questGraph in _quests) {
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
        val graph = _json.fromJson(QuestGraph::class.java, Gdx.files.internal(questConfigPath))
        if (doesQuestExist(graph.questID)) {
            return null
        }
        clearDialog()
        _quests.add(graph)
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
        for (questGraph in _quests) {
            if (questGraph.questID.equals(questGraphID, ignoreCase = true)) {
                return questGraph
            }
        }
        return null
    }

    fun doesQuestExist(questGraphID: String?): Boolean {
        for (questGraph in _quests) {
            if (questGraph.questID.equals(questGraphID, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    var quests: Array<QuestGraph>
        get() = _quests
        set(quests) {
            _quests = quests
            updateQuestItemList()
        }

    fun updateQuestItemList() {
        clearDialog()
        _listQuests.setItems(_quests)
        _listQuests.selectedIndex = -1
    }

    private fun clearDialog() {
        _listQuests.clearItems()
        _listTasks.clearItems()
    }

    private fun populateQuestTaskDialog(graph: QuestGraph) {
        _listTasks.clearItems()
        val tasks = graph.allQuestTasks
        _listTasks.setItems(*tasks.toTypedArray())
        _listTasks.selectedIndex = -1
    }

    fun initQuests(mapMgr: MapManager) {
        mapMgr.clearAllMapQuestEntities()

        //populate items if quests have them
        for (quest in _quests) {
            if (!quest.isQuestComplete) {
                quest.init(mapMgr)
            }
        }
        ProfileManager.instance!!.setProperty("playerQuests", _quests)
    }

    fun updateQuests(mapMgr: MapManager?) {
        for (quest in _quests) {
            if (!quest.isQuestComplete) {
                quest.update(mapMgr!!)
            }
        }
        ProfileManager.instance!!.setProperty("playerQuests", _quests)
    }

    companion object {
        private val TAG = QuestUI::class.java.simpleName
        const val RETURN_QUEST = "conversations/return_quest.json"
        const val FINISHED_QUEST = "conversations/quest_finished.json"
    }

    init {
        _json = Json()
        _quests = Array()

        //create
        _questLabel = Label("Quests:", Utility.STATUSUI_SKIN)
        _tasksLabel = Label("Tasks:", Utility.STATUSUI_SKIN)
        _listQuests = List<QuestGraph>(Utility.STATUSUI_SKIN)
        val scrollPane = ScrollPane(_listQuests, Utility.STATUSUI_SKIN, "inventoryPane")
        scrollPane.setOverscroll(false, false)
        scrollPane.setFadeScrollBars(false)
        scrollPane.setForceScroll(true, false)
        _listTasks = List<QuestTask>(Utility.STATUSUI_SKIN)
        val scrollPaneTasks = ScrollPane(_listTasks, Utility.STATUSUI_SKIN, "inventoryPane")
        scrollPaneTasks.setOverscroll(false, false)
        scrollPaneTasks.setFadeScrollBars(false)
        scrollPaneTasks.setForceScroll(true, false)

        //layout
        this.add(_questLabel).align(Align.left)
        this.add(_tasksLabel).align(Align.left)
        row()
        defaults().expand().fill()
        this.add(scrollPane).padRight(15f)
        this.add(scrollPaneTasks).padLeft(5f)

        //this.debug();
        pack()

        //Listeners
        _listQuests.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                val quest = _listQuests.selected as QuestGraph ?: return
                populateQuestTaskDialog(quest)
            }
        }
        )
    }
}