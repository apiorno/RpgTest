package com.mygdx.game.ecs

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.mygdx.game.ecs.Component.MESSAGE
import com.mygdx.game.maps.MapManager

class PlayerGraphicsComponent : GraphicsComponent() {

    private var previousPosition = Vector2(0F, 0F)

    override fun receiveMessage(message: String) {
        //Gdx.app.debug(TAG, "Got message " + message);
        val string: Array<String> = message.split(Component.MESSAGE_TOKEN).toTypedArray()
        if (string.isEmpty()) return

        //Specifically for messages with 1 object payload
        if (string.size == 2) {
            when {
                string[0].equals(MESSAGE.CURRENT_POSITION.toString(), ignoreCase = true) -> {
                    currentPosition = json.fromJson(Vector2::class.java, string[1])
                }
                string[0].equals(MESSAGE.INIT_START_POSITION.toString(), ignoreCase = true) -> {
                    currentPosition = json.fromJson(Vector2::class.java, string[1])
                }
                string[0].equals(MESSAGE.CURRENT_STATE.toString(), ignoreCase = true) -> {
                    currentState = json.fromJson(Entity.State::class.java, string[1])
                }
                string[0].equals(MESSAGE.CURRENT_DIRECTION.toString(), ignoreCase = true) -> {
                    currentDirection = json.fromJson(Entity.Direction::class.java, string[1])
                }
                string[0].equals(MESSAGE.LOAD_ANIMATIONS.toString(), ignoreCase = true) -> {
                    val entityConfig = json.fromJson(EntityConfig::class.java, string[1])
                    val animationConfigs = entityConfig.animationConfig
                    animationConfigs.forEach {
                        val textureNames = it.texturePaths
                        val points = it.gridPoints
                        val animationType = it.animationType
                        val frameDuration = it.frameDuration
                        var animation: Animation<TextureRegion>? = null
                        if (textureNames.size == 1) {
                            animation = loadAnimation(textureNames[0]!!, points, frameDuration)
                        } else if (textureNames.size == 2) {
                            animation = loadAnimation(textureNames[0]!!, textureNames[1]!!, points, frameDuration)
                        }
                        animations[animationType] = animation
                    }
                }
            }
        }
    }

    override fun update(entity: Entity, mapManager: MapManager, batch: Batch, delta: Float) {
        updateAnimations(delta)

        //Player has moved
        if (previousPosition.x != currentPosition.x ||
                previousPosition.y != currentPosition.y) {
            notify("", ComponentObserver.ComponentEvent.PLAYER_HAS_MOVED)
            previousPosition = currentPosition.cpy()
        }
        val camera = mapManager.camera
        camera.position[currentPosition.x, currentPosition.y] = 0f
        camera.update()
        batch.begin()
        batch.draw(currentFrame, currentPosition.x, currentPosition.y, 1f, 1f)
        batch.end()
    }

    override fun dispose() {}

    companion object {
        private val TAG = PlayerGraphicsComponent::class.java.simpleName
    }
}