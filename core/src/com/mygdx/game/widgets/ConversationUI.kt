package com.mygdx.game.widgets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Json
import com.mygdx.game.ecs.EntityConfig
import com.mygdx.game.Utility
import com.mygdx.game.dialog.ConversationChoice
import com.mygdx.game.dialog.ConversationGraph

class ConversationUI : Window("dialog", Utility.STATUSUI_SKIN, "solidbackground") {
    private val _dialogText: Label
    private val _listItems: List<ConversationChoice>
    var currentConversationGraph: ConversationGraph?
        private set
    var currentEntityID: String? = null
        private set
    val closeButton: TextButton
    private val _json: Json

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
        val graph = _json.fromJson(ConversationGraph::class.java, Gdx.files.internal(fullFilenamePath))
        setConversationGraph(graph)
    }

    fun setConversationGraph(graph: ConversationGraph?) {
        if (currentConversationGraph != null) currentConversationGraph!!.removeAllObservers()
        currentConversationGraph = graph
        populateConversationDialog(currentConversationGraph!!.currentConversationID)
    }

    private fun populateConversationDialog(conversationID: String?) {
        clearDialog()
        val conversation = currentConversationGraph!!.getConversationByID(conversationID) ?: return
        currentConversationGraph!!.setCurrentConversation(conversationID)
        _dialogText.setText(conversation.dialog)
        val choices = currentConversationGraph!!.currentChoices
        _listItems.setItems(*choices.toTypedArray())
        _listItems.selectedIndex = -1
    }

    private fun clearDialog() {
        _dialogText.setText("")
        _listItems.clearItems()
    }

    companion object {
        private val TAG = ConversationUI::class.java.simpleName
    }

    init {
        _json = Json()
        currentConversationGraph = ConversationGraph()

        //create
        _dialogText = Label("No Conversation", Utility.STATUSUI_SKIN)
        _dialogText.setWrap(true)
        _dialogText.setAlignment(Align.center)
        _listItems = List<ConversationChoice>(Utility.STATUSUI_SKIN)
        closeButton = TextButton("X", Utility.STATUSUI_SKIN)
        val scrollPane = ScrollPane(_listItems, Utility.STATUSUI_SKIN, "inventoryPane")
        scrollPane.setOverscroll(false, false)
        scrollPane.setFadeScrollBars(false)
        scrollPane.setScrollingDisabled(true, false)
        scrollPane.setForceScroll(true, false)
        scrollPane.setScrollBarPositions(false, true)

        //layout
        this.add()
        this.add(closeButton)
        row()
        defaults().expand().fill()
        this.add(_dialogText).pad(10f, 10f, 10f, 10f)
        row()
        this.add(scrollPane).pad(10f, 10f, 10f, 10f)

        //this.debug();
        pack()

        //Listeners
        _listItems.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                val choice = _listItems.selected as ConversationChoice
                currentConversationGraph!!.notify(currentConversationGraph!!, choice.conversationCommandEvent!!)
                populateConversationDialog(choice.destinationId)
            }
        }
        )
    }
}