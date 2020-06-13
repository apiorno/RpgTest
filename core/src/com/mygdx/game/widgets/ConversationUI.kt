package com.mygdx.game.widgets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Window
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.mygdx.game.EntityConfig
import com.mygdx.game.Utility
import com.mygdx.game.dialog.ConversationChoice
import com.mygdx.game.dialog.ConversationGraph

class ConversationUI : Window("dialog", Utility.STATUSUI_SKIN, "solidbackground") {
    private val dialogText: Label
    private val listItems: com.badlogic.gdx.scenes.scene2d.ui.List<*>
    var currentConversationGraph: ConversationGraph?
        private set
    var currentEntityID: String? = null
        private set
    val closeButton: TextButton
    private val json: Json

    fun loadConversation(entityConfig: EntityConfig) {
        val fullFilenamePath = entityConfig.conversationConfigPath
        titleLabel.setText("")
        clearDialog()
        if (fullFilenamePath!!.isEmpty() || !Gdx.files.internal(fullFilenamePath).exists()) {
            Gdx.app.debug(TAG, "Conversation file does not exist!")
            return
        }
        currentEntityID = entityConfig.entityID
        titleLabel.setText(entityConfig.entityID)
        val graph = json.fromJson(ConversationGraph::class.java, Gdx.files.internal(fullFilenamePath))
        setConversationGraph(graph)
    }

    private fun setConversationGraph(graph: ConversationGraph?) {
        if (currentConversationGraph != null) currentConversationGraph!!.removeAllObservers()
        currentConversationGraph = graph
        populateConversationDialog(currentConversationGraph!!.currentConversationID)
    }

    private fun populateConversationDialog(conversationID: String?) {
        clearDialog()
        val conversation = currentConversationGraph!!.getConversationByID(conversationID) ?: return
        currentConversationGraph!!.setCurrentConversation(conversationID)
        dialogText.setText(conversation.dialog)
        val choices = currentConversationGraph!!.currentChoices as Array<ConversationChoice>
        listItems.setItems(choices)
        listItems.selectedIndex = -1
    }

    private fun clearDialog() {
        dialogText.setText("")
        listItems.clearItems()
    }

    companion object {
        private val TAG = ConversationUI::class.java.simpleName
    }

    init {
        json = Json()
        currentConversationGraph = ConversationGraph()

        //create
        dialogText = Label("No Conversation", Utility.STATUSUI_SKIN)
        dialogText.setWrap(true)
        dialogText.setAlignment(Align.center)
        listItems = List<ConversationChoice>(Utility.STATUSUI_SKIN)
        closeButton = TextButton("X", Utility.STATUSUI_SKIN)
        val scrollPane = ScrollPane(listItems, Utility.STATUSUI_SKIN, "inventoryPane")
        scrollPane.setOverscroll(false, false)
        scrollPane.fadeScrollBars = false
        scrollPane.setScrollingDisabled(true, false)
        scrollPane.setForceScroll(true, false)
        scrollPane.setScrollBarPositions(false, true)

        //layout
        this.add()
        this.add(closeButton)
        row()
        defaults().expand().fill()
        this.add(dialogText).pad(10f, 10f, 10f, 10f)
        row()
        this.add(scrollPane).pad(10f, 10f, 10f, 10f)

        //this.debug();
        pack()

        //Listeners
        listItems.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                val choice = listItems.selected as ConversationChoice ?: return
                currentConversationGraph!!.notify(currentConversationGraph, choice.conversationCommandEvent)
                populateConversationDialog(choice.destinationId)
            }
        }
        )
    }
}