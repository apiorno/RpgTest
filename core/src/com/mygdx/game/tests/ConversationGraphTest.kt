package com.mygdx.game.tests

import com.mygdx.game.dialog.Conversation
import com.mygdx.game.dialog.ConversationChoice
import com.mygdx.game.dialog.ConversationGraph
import java.util.*

object ConversationGraphTest {
    var _conversations: Hashtable<String, Conversation>? = null
    var _graph: ConversationGraph? = null
    var quit = "q"
    var _input = ""

    @JvmStatic
    fun main(arg: Array<String>) {
        _conversations = Hashtable()
        val start = Conversation()
        start.id = "500"
        start.dialog = "Do you want to play a game?"
        val yesAnswer = Conversation()
        yesAnswer.id = "601"
        yesAnswer.dialog = "BOOM! Bombs dropping everywhere"
        val noAnswer = Conversation()
        noAnswer.id = "802"
        noAnswer.dialog = "Too bad!"
        val unconnectedTest = Conversation()
        unconnectedTest.id = "250"
        unconnectedTest.dialog = "I am unconnected"
        _conversations!![start.id] = start
        _conversations!![noAnswer.id] = noAnswer
        _conversations!![yesAnswer.id] = yesAnswer
        _conversations!![unconnectedTest.id] = unconnectedTest
        _graph = ConversationGraph(_conversations, start.id)
        val yesChoice = ConversationChoice()
        yesChoice.sourceId = start.id
        yesChoice.destinationId = yesAnswer.id
        yesChoice.choicePhrase = "YES"
        val noChoice = ConversationChoice()
        noChoice.sourceId = start.id
        noChoice.destinationId = noAnswer.id
        noChoice.choicePhrase = "NO"
        val startChoice01 = ConversationChoice()
        startChoice01.sourceId = yesAnswer.id
        startChoice01.destinationId = start.id
        startChoice01.choicePhrase = "Go to beginning!"
        val startChoice02 = ConversationChoice()
        startChoice02.sourceId = noAnswer.id
        startChoice02.destinationId = start.id
        startChoice02.choicePhrase = "Go to beginning!"
        _graph!!.addChoice(yesChoice)
        _graph!!.addChoice(noChoice)
        _graph!!.addChoice(startChoice01)
        _graph!!.addChoice(startChoice02)

        //System.out.println(_graph.toString());
        //System.out.println(_graph.displayCurrentConversation());
        //System.out.println(_graph.toJson());
        while (!_input.equals(quit, ignoreCase = true)) {
            val conversation = nextChoice ?: continue
            _graph!!.setCurrentConversation(conversation.id)
            //System.out.println(_graph.displayCurrentConversation());
        }
    }

    //System.out.println(choice.getDestinationId() + " " + choice.getChoicePhrase());
    val nextChoice: Conversation?
        get() {
            val choices = _graph!!.currentChoices
            for (choice in choices) {
                //System.out.println(choice.getDestinationId() + " " + choice.getChoicePhrase());
            }
            _input = System.console().readLine()
            var choice: Conversation? = null
            choice = try {
                _graph!!.getConversationByID(_input)
            } catch (nfe: NumberFormatException) {
                return null
            }
            return choice
        }
}