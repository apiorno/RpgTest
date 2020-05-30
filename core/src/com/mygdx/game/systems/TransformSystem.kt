package com.mygdx.game.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.google.inject.Inject
import com.mygdx.game.*

class TransformSystem @Inject constructor() : IteratingSystem(Family.all(TransformComponent::class.java,StateComponent::class.java).get()) {


    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transformComponent = entity.transform
        val stateComponent = entity.state
        if (deltaTime > .7) return
        var newX = transformComponent.position.x
        var newY = transformComponent.position.y
        transformComponent.velocity.scl(deltaTime)
        if (stateComponent.state == "WALKING") {
            when (stateComponent.direction) {
                "LEFT" -> newX -= transformComponent.velocity.x
                "RIGHT" -> newX += transformComponent.velocity.x
                "UP" -> newY += transformComponent.velocity.y
                "DOWN" -> newY -= transformComponent.velocity.y
                else -> {
                }
            }
        }
        transformComponent.position.x = newX
        transformComponent.position.y = newY

        //velocity
        transformComponent.velocity.scl(1 / deltaTime)
    }


}