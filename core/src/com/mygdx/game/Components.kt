package com.mygdx.game

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.utils.ArrayMap
import java.util.*


class TransformComponent(val position: Vector2, var angleRadians: Float, var scale: Float, var velocity: Vector2) : Component {
    constructor(position: Vector2) : this(position, 0F, 1F, Vector2(2f, 2f))
    constructor(position: Vector2, angleRadians: Float,scale: Float) : this(position, angleRadians, scale, Vector2(2f, 2f))
    companion object : ComponentResolver<TransformComponent>(TransformComponent::class.java)
}
val Entity.transform : TransformComponent
    get() = TransformComponent[this]

class TextureComponent(val texture: Texture) : Component {
    companion object : ComponentResolver<TextureComponent>(TextureComponent::class.java)
}

val Entity.texture : TextureComponent
    get() = TextureComponent[this]

class TextureRegionComponent(var textureRegion: TextureRegion) : Component {
    companion object : ComponentResolver<TextureRegionComponent>(TextureRegionComponent::class.java)
}

val Entity.textureRegion : TextureRegionComponent
    get() = TextureRegionComponent[this]

class PhysicsComponent(val body: Body) : Component {
    companion object : ComponentResolver<PhysicsComponent>(PhysicsComponent::class.java)
}

val Entity.physics : PhysicsComponent
    get() = PhysicsComponent[this]

class AnimationComponent(var animations: Hashtable<String, Animation<TextureRegion?>>) : Component {
    companion object : ComponentResolver<AnimationComponent>(AnimationComponent::class.java)
}

val Entity.animation : AnimationComponent
    get() = AnimationComponent[this]

class StateComponent(var state: String, var direction: String , var frameTime: Float) : Component {
    constructor() : this("IMMOBILE", "DOWN", 0F)
    companion object : ComponentResolver<StateComponent>(StateComponent::class.java)
}

val Entity.state : StateComponent
    get() = StateComponent[this]

class InputComponent : Component {
    companion object : ComponentResolver<InputComponent>(InputComponent::class.java)
}

val Entity.input : InputComponent
    get() = InputComponent[this]


open class ComponentResolver <T : Component> (componentClass: Class<T>){
    val MAPPER = ComponentMapper.getFor(componentClass)
    operator fun get(entity: Entity)= MAPPER.get(entity)
}

fun <T : Component> Entity.tryGet (componentResolver :ComponentResolver <T>) : T?{
    return componentResolver.MAPPER.get(this)
}