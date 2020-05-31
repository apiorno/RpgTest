package com.mygdx.game.temporal

import AnimationType
import MESSAGE
import MESSAGE_TOKEN
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue
import com.mygdx.game.EntityConfig
import com.mygdx.game.temporal.EntityFactory.*
import com.mygdx.game.profile.ProfileManager
import java.util.*

class Entity {
    private var json: Json? = null
    var entityConfig: EntityConfig? = null
    private var components: Array<Component?>? = null
    private var inputComponent: InputComponent? = null
    private var graphicsComponent: GraphicsComponent? = null
    private var physicsComponent: PhysicsComponent? = null

    constructor(entity: Entity) {
        set(entity)
    }

    private fun set(entity: Entity): Entity {
        inputComponent = entity.inputComponent
        graphicsComponent = entity.graphicsComponent
        physicsComponent = entity.physicsComponent
        if (components == null) {
            components = Array(MAX_COMPONENTS)
        }
        components!!.clear()
        components!!.add(inputComponent)
        components!!.add(physicsComponent)
        components!!.add(graphicsComponent)
        json = entity.json
        entityConfig = EntityConfig(entity.entityConfig)
        return this
    }

    constructor(inputComponent: InputComponent?, physicsComponent: PhysicsComponent?, graphicsComponent: GraphicsComponent?) {
        entityConfig = EntityConfig()
        json = Json()
        components = Array(MAX_COMPONENTS)
        inputComponent = inputComponent
        physicsComponent = physicsComponent
        graphicsComponent = graphicsComponent
        components!!.add(inputComponent)
        components!!.add(physicsComponent)
        components!!.add(graphicsComponent)
    }

    fun sendMessage(messageType: MESSAGE, vararg args: String) {
        var fullMessage = messageType.toString()
        for (string in args) {
            fullMessage += MESSAGE_TOKEN + string
        }
        for (component in components!!) {
            component!!.receiveMessage(fullMessage)
        }
    }

    fun registerObserver(observer: ComponentObserver?) {
        inputComponent!!.addObserver(observer!!)
        physicsComponent!!.addObserver(observer)
        graphicsComponent!!.addObserver(observer)
    }

    fun unregisterObservers() {
        inputComponent!!.removeAllObservers()
        physicsComponent!!.removeAllObservers()
        graphicsComponent!!.removeAllObservers()
    }

    fun update(mapMgr: MapManager, batch: Batch, delta: Float) {
        inputComponent!!.update(this, delta)
        physicsComponent!!.update(this, mapMgr, delta)
        graphicsComponent!!.update(this, mapMgr, batch, delta)
    }

    fun updateInput(delta: Float) {
        inputComponent!!.update(this, delta)
    }

    fun dispose() {
        for (component in components!!) {
            component!!.dispose()
        }
    }

    val currentBoundingBox: Rectangle?
        get() = physicsComponent!!._boundingBox

    val currentPosition: Vector2?
        get() = graphicsComponent!!._currentPosition

    val inputProcessor: InputProcessor?
        get() = inputComponent

    fun getAnimation(type: AnimationType?): Animation<TextureRegion?>? {
        return graphicsComponent!!.getAnimation(type)
    }


}