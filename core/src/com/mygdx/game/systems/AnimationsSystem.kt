package com.mygdx.game.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.google.inject.Inject
import com.mygdx.game.*

class AnimationsSystem @Inject constructor() : IteratingSystem(Family.all(StateComponent::class.java, AnimationComponent::class.java, TextureRegionComponent::class.java).get()){
    override fun processEntity(entity: Entity, deltaTime: Float) {

        val texture = entity.textureRegion
        val animations = entity.animation.animations
        val state = entity.state


        when(state.direction){
            "DOWN" -> when(state.state){
                "WALKING" -> {val currentAnimation = animations["WALK_DOWN"] ?: return
                texture.textureRegion = currentAnimation.getKeyFrame(state.frameTime)!!
                }
                "IDLE" -> {val currentAnimation = animations["WALK_DOWN"] ?: return
                    texture.textureRegion = currentAnimation.getKeyFrame(0F)!!
                }
                "IMMOBILE" -> {val currentAnimation = animations["IMMOBILE"] ?: return
                    texture.textureRegion = currentAnimation.getKeyFrame(state.frameTime)!!
                }
            }
            "LEFT" -> when(state.state){
                "WALKING" -> {val currentAnimation = animations["WALK_LEFT"] ?: return
                    texture.textureRegion = currentAnimation.getKeyFrame(state.frameTime)!!
                }
                "IDLE" -> {val currentAnimation = animations["WALK_LEFT"] ?: return
                    texture.textureRegion = currentAnimation.getKeyFrame(0F)!!
                }
                "IMMOBILE" -> {val currentAnimation = animations["IMMOBILE"] ?: return
                    texture.textureRegion = currentAnimation.getKeyFrame(state.frameTime)!!
                }
            }
            "UP" -> when(state.state){
                "WALKING" -> {val currentAnimation = animations["WALK_UP"] ?: return
                    texture.textureRegion = currentAnimation.getKeyFrame(state.frameTime)!!
                }
                "IDLE" -> {val currentAnimation = animations["WALK_UP"] ?: return
                    texture.textureRegion = currentAnimation.getKeyFrame(0F)!!
                }
                "IMMOBILE" -> {val currentAnimation = animations["IMMOBILE"] ?: return
                    texture.textureRegion = currentAnimation.getKeyFrame(state.frameTime)!!
                }
            }
            "RIGHT" -> when(state.state){
                "WALKING" -> {val currentAnimation = animations["WALK_RIGHT"] ?: return
                    texture.textureRegion = currentAnimation.getKeyFrame(state.frameTime)!!
                }
                "IDLE" -> {val currentAnimation = animations["WALK_RIGHT"] ?: return
                    texture.textureRegion = currentAnimation.getKeyFrame(0F)!!
                }
                "IMMOBILE" -> {val currentAnimation = animations["IMMOBILE"] ?: return
                    texture.textureRegion = currentAnimation.getKeyFrame(state.frameTime)!!
                }
            }
            else -> {
            }
        }
        state.frameTime = (state.frameTime + deltaTime) % 5 //Want to avoid overflow
    }
}