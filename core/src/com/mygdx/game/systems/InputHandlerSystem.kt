package com.mygdx.game.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.google.inject.Inject
import com.mygdx.game.*
import com.mygdx.game.PlayerInputProcessor.*


class InputHandlerSystem @Inject constructor() : IteratingSystem(Family.all(InputComponent::class.java).get()), IObserver {
     var presedKeys  = HashMap<PlayerInputProcessor.Key,Boolean>()

    override fun receiveNotification(event: Event) {
        presedKeys[event.value] = event.eventType == EventType.INPUT_PRESSED
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {

        if(isPressedKey(Key.QUIT)){
            Gdx.app.exit()
        }
        if(isPressedKey(Key.PAUSE)){
            //Pause game
        }
        val stateComponent = entity.state
        stateComponent.state = "IDLE"

        if(isPressedKey(Key.RIGHT)){
            stateComponent.state = "WALKING"
            stateComponent.direction = "RIGHT"
        }
        if(isPressedKey(Key.LEFT)){
            stateComponent.state = "WALKING"
            stateComponent.direction = "LEFT"
        }
        if(isPressedKey(Key.DOWN)){
            stateComponent.state = "WALKING"
            stateComponent.direction = "DOWN"
        }
        if(isPressedKey(Key.UP)){
            stateComponent.state = "WALKING"
            stateComponent.direction = "UP"
        }
    }
    private fun isPressedKey(key: Key):Boolean{
       val pressed : Boolean?= presedKeys[key]
        return pressed!=null && pressed
    }


}