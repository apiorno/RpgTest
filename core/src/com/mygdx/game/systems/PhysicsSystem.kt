package com.mygdx.game.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.physics.box2d.World
import com.google.inject.Inject

class PhysicsSystem @Inject constructor(private val world: World) : EntitySystem() {
    private var accumulator = 0f
    override fun update(deltaTime: Float) {
        // fixed time step
        // max frame time to avoid spiral of death (on slow devices)
        // fixed time step
        // max frame time to avoid spiral of death (on slow devices)
        //https://github.com/libgdx/libgdx/wiki/Box2d#stepping-the-simulation
        //https://gafferongames.com/post/fix_your_timestep/
        val frameTime = Math.min(deltaTime, 0.25f)
        accumulator += frameTime
        while (accumulator >= TIME_STEP) {
            world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS)
            accumulator -= TIME_STEP
        }
    }
    companion object{
        private val TIME_STEP = 1.0F/300F
        private val VELOCITY_ITERATIONS = 6
        private val POSITION_ITERATIONS = 2
    }
}
