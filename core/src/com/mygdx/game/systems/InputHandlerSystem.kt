package com.mygdx.game.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.google.inject.Inject
import com.mygdx.game.*


class InputHandlerSystem @Inject constructor() : IteratingSystem(Family.all(InputComponent::class.java).get()), IObserver {
     var lastEvent  : Event? = null

    override fun receiveNotification(event: Event) {
        lastEvent = event
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (lastEvent != null) {

            when (lastEvent!!.value) {
                "QUIT" -> {
                    lastEvent = null
                    Gdx.app.exit()
                }
                "PAUSE" -> {
                    //Pause game

                }
                else -> {
                    val stateComponent = entity.state
                    stateComponent.direction = lastEvent!!.value
                    if(lastEvent!!.eventType == EventType.INPUT_PRESSED){
                        stateComponent.state = "WALKING"
                    }
                    if(lastEvent!!.eventType == EventType.INPUT_RELEASED) {
                        stateComponent.state = "IDLE"
                    }
                    lastEvent = null
                }
            }
        }

    }


}