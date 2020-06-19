package com.mygdx.game.dialog

import com.badlogic.gdx.utils.Json
import java.util.*

class ConversationGraph : ConversationGraphSubject {
    private var conversations: Hashtable<String, Conversation>? = null
    private var associatedChoices: Hashtable<String, ArrayList<ConversationChoice>>? = null
    var currentConversationID: String? = null
        private set

    constructor() {}
    constructor(conversations: Hashtable<String, Conversation>?, rootID: String?) {
        setConversations(conversations)
        setCurrentConversation(rootID)
    }

    fun setConversations(conversations: Hashtable<String, Conversation>?) {
        require(conversations?.size!! >= 0) { "Can't have a negative amount of conversations" }
        this.conversations = conversations
        associatedChoices = Hashtable(conversations.size)
        for (conversation in conversations.values) {
            associatedChoices!![conversation?.id] = ArrayList()
        }
    }

    val currentChoices: ArrayList<ConversationChoice>
        get() = associatedChoices!![currentConversationID]!!

    fun setCurrentConversation(id: String?) {
        val conversation = getConversationByID(id) ?: return
        //Can we reach the new conversation from the current one?

        //Make sure we check case where the current node is checked against itself
        if (currentConversationID == null ||
                currentConversationID.equals(id, ignoreCase = true) ||
                isReachable(currentConversationID, id)) {
            currentConversationID = id
        } else {
            //System.out.println("New conversation node [" + id +"] is not reachable from current node [" + currentConversationID + "]");
        }
    }

    fun isValid(conversationID: String?): Boolean {
        val conversation = conversations!![conversationID] ?: return false
        return true
    }

    fun isReachable(sourceID: String?, sinkID: String?): Boolean {
        if (!isValid(sourceID) || !isValid(sinkID)) return false
        if (conversations!![sourceID] == null) return false

        //First get edges/choices from the source
        val list = associatedChoices!![sourceID] ?: return false
        for (choice in list) {
            if (choice.sourceId.equals(sourceID, ignoreCase = true) &&
                    choice.destinationId.equals(sinkID, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    fun getConversationByID(id: String?): Conversation? {
        return if (!isValid(id)) {
            //System.out.println("Id " + id + " is not valid!");
            null
        } else conversations!![id]
    }

    fun displayCurrentConversation(): String? {
        return conversations!![currentConversationID]?.dialog
    }

    fun addChoice(conversationChoice: ConversationChoice) {
        val list = associatedChoices!![conversationChoice.sourceId] ?: return
        list.add(conversationChoice)
    }

    override fun toString(): String {
        val outputString = StringBuilder()
        var numberTotalChoices = 0
        val keys: Set<String?> = associatedChoices!!.keys
        for (id in keys) {
            outputString.append(String.format("[%s]: ", id))
            for (choice in associatedChoices!![id]!!) {
                numberTotalChoices++
                outputString.append(String.format("%s ", choice.destinationId))
            }
            outputString.append(System.getProperty("line.separator"))
        }
        outputString.append(String.format("Number conversations: %d", conversations!!.size))
        outputString.append(String.format(", Number of choices: %d", numberTotalChoices))
        outputString.append(System.getProperty("line.separator"))
        return outputString.toString()
    }

    fun toJson(): String {
        val json = Json()
        return json.prettyPrint(this)
    }
}