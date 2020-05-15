package com.mygdx.game

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.World
import com.google.inject.Binder
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.Singleton
import com.mygdx.game.systems.*

class GameModule(private val myGdxGame: MyGdxGame) : Module {
    private val UNIT_SCALE = 1/8F
    override fun configure(binder: Binder) {
        binder.requireAtInjectOnConstructors()
        binder.requireExactBindingAnnotations()
        binder.bind(SpriteBatch::class.java).toInstance(myGdxGame.batch)
    }

    @Provides
    @Singleton
    fun systems (): Systems {
        return Systems(listOf(
                PhysicsSystem::class.java,
                PhysicsSynchronizationSystem::class.java,
                InputHandlerSystem::class.java,
                AnimationsSystem::class.java,
                TransformSystem::class.java,
                RenderingSystem::class.java,
                PhysicsDebugSystem::class.java
        ))
    }
    @Provides
    @Singleton
    fun camera () : OrthographicCamera {
        val viewportWidth =  Gdx.graphics.width.pixelToMeters
        val viewportHeight = Gdx.graphics.height.pixelToMeters
        return OrthographicCamera(viewportWidth, viewportHeight).apply {
            position.set(viewportWidth/2F,viewportHeight/2F,0F)
            update()
        }
    }
    @Provides
    @Singleton
    fun mapRenderer() : OrthogonalTiledMapRenderer {
        val map = TmxMapLoader().load("maps/town.tmx")
        return OrthogonalTiledMapRenderer(map,UNIT_SCALE)
    }

    @Provides
    @Singleton
    fun world () : World {
        Box2D.init()
        return World(Vector2(0F, -9.81F), true)
    }

    @Provides
    @Singleton
    fun engine () : Engine {
        return Engine()
    }
    @Provides
    @Singleton
    fun assetManager () : AssetManager {
        return AssetManager()
    }

    @Provides
    @Singleton
    fun inputProcessor () : PlayerInputProcessor {
        return PlayerInputProcessor()
    }

}

data class Systems(val list: List<Class<out EntitySystem>>)