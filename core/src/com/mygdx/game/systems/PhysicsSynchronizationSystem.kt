package com.mygdx.game.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.google.inject.Inject
import com.mygdx.game.PhysicsComponent
import com.mygdx.game.TransformComponent
import com.mygdx.game.physics
import com.mygdx.game.transform

class PhysicsSynchronizationSystem @Inject constructor(): IteratingSystem(Family.all(TransformComponent::class.java, PhysicsComponent::class.java).get()){
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val body = entity.physics.body
        entity.transform.position.set(body.position)
        entity.transform.angleRadians = body.angle
    }
}