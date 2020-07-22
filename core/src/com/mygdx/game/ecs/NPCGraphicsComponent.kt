package com.mygdx.game.ecs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.mygdx.game.ecs.Component.MESSAGE
import com.mygdx.game.maps.Map
import com.mygdx.game.maps.MapManager

class NPCGraphicsComponent : GraphicsComponent() {

    private var isSelected = false
    private var wasSelected = false
    private var sentShowConversationMessage = false
    private var sentHideCoversationMessage = false

    override fun receiveMessage(message: String) {
        //Gdx.app.debug(TAG, "Got message " + message);
        val string: Array<String> = message.split(Component.MESSAGE_TOKEN).toTypedArray()
        if (string.isEmpty()) return
        if (string.size == 1) {
            if (string[0].equals(MESSAGE.ENTITY_SELECTED.toString(), ignoreCase = true)) {
                isSelected = !wasSelected
            } else if (string[0].equals(MESSAGE.ENTITY_DESELECTED.toString(), ignoreCase = true)) {
                wasSelected = isSelected
                isSelected = false
            }
        }
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
        if (isSelected) {
            drawSelected(entity, mapManager)
            mapManager.currentSelectedMapEntity = entity
            if (!sentShowConversationMessage) {
                notify(json.toJson(entity.entityConfig), ComponentObserver.ComponentEvent.SHOW_CONVERSATION)
                sentShowConversationMessage = true
                sentHideCoversationMessage = false
            }
        } else {
            if (!sentHideCoversationMessage) {
                notify(json.toJson(entity.entityConfig), ComponentObserver.ComponentEvent.HIDE_CONVERSATION)
                sentHideCoversationMessage = true
                sentShowConversationMessage = false
            }
        }
        batch.begin()
        batch.draw(currentFrame, currentPosition.x, currentPosition.y, 1f, 1f)
        batch.end()
    }

    private fun drawSelected(entity: Entity, mapMgr: MapManager) {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        val camera = mapMgr.camera
        val rect = entity.currentBoundingBox
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.setColor(0.0f, 1.0f, 1.0f, 0.5f)
        val width: Float = rect!!.getWidth() * Map.UNIT_SCALE * 2f
        val height: Float = rect.getHeight() * Map.UNIT_SCALE / 2f
        val x: Float = rect.x * Map.UNIT_SCALE - width / 4
        val y: Float = rect.y * Map.UNIT_SCALE - height / 2
        shapeRenderer.ellipse(x, y, width, height)
        shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    override fun dispose() {}

    companion object {
        private val TAG = NPCGraphicsComponent::class.java.simpleName
    }
}