package com.mygdx.game.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.google.inject.Inject
import com.mygdx.game.*


class InputHandlerSystem @Inject constructor() : IteratingSystem(Family.all(InputComponent::class.java).get()), IObserver {
     var presedKeys  = HashMap<PlayerInputProcessor.Key,Boolean>()

    override fun receiveNotification(event: Event) {
        presedKeys[event.value] = event.eventType == EventType.INPUT_PRESSED
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {

        if(presedKeys[PlayerInputProcessor.Key.QUIT]!!){
            Gdx.app.exit()
        }
        if(presedKeys[PlayerInputProcessor.Key.PAUSE]!!){
            //Pause game
        }
        val stateComponent = entity.state
        stateComponent.state = "IDLE"

        if(presedKeys[PlayerInputProcessor.Key.RIGHT]!!){
            stateComponent.state = "WALKING"
            stateComponent.direction = "RIGHT"
        }
        if(presedKeys[PlayerInputProcessor.Key.LEFT]!!){
            stateComponent.state = "WALKING"
            stateComponent.direction = "LEFT"
        }
        if(presedKeys[PlayerInputProcessor.Key.DOWN]!!){
            stateComponent.state = "WALKING"
            stateComponent.direction = "DOWN"
        }
        if(presedKeys[PlayerInputProcessor.Key.UP]!!){
            stateComponent.state = "WALKING"
            stateComponent.direction = "UP"
        }
    }


}