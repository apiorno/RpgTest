package com.mygdx.game.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.google.inject.Inject
import com.mygdx.game.*

class RenderingSystem  @Inject constructor(private val batch: SpriteBatch, private val camera: OrthographicCamera, private val mapRenderer: OrthogonalTiledMapRenderer) : IteratingSystem(Family.all(TransformComponent::class.java).one(TextureComponent::class.java, TextureRegionComponent::class.java).get()){
    override fun update(deltaTime: Float) {
        batch.projectionMatrix = camera.combined
        mapRenderer.setView(camera);
        mapRenderer.render();
        batch.begin()
        super.update(deltaTime)
        batch.end()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val position = entity.transform.position

        entity.tryGet(TextureComponent)?.let { textureComponent ->
            val img = textureComponent.texture

            batch.draw(img, position.x - img.width.pixelToMeters/2F,position.y - img.height.pixelToMeters/2F, img.width.pixelToMeters,img.height.pixelToMeters)
        }
        entity.tryGet(TextureRegionComponent)?.let { textureRegionComponent ->
            val img = textureRegionComponent.textureRegion
            val width = img.regionWidth.pixelToMeters
            val height = img.regionHeight.pixelToMeters
            val scale = entity.transform.scale

            batch.draw(img, position.x - width /2,position.y - height /2, width/2F,height/2F,
                    width, height, scale, scale, entity.transform.angleRadians.toDegrees)
        }


    }

}