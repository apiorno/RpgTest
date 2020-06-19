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
    private var _isSelected = false
    private var _wasSelected = false
    private var _sentShowConversationMessage = false
    private var _sentHideCoversationMessage = false
    override fun receiveMessage(message: String) {
        //Gdx.app.debug(TAG, "Got message " + message);
        val string: Array<String> = message.split(Component.Companion.MESSAGE_TOKEN).toTypedArray()
        if (string.size == 0) return
        if (string.size == 1) {
            if (string[0].equals(MESSAGE.ENTITY_SELECTED.toString(), ignoreCase = true)) {
                _isSelected = !_wasSelected
            } else if (string[0].equals(MESSAGE.ENTITY_DESELECTED.toString(), ignoreCase = true)) {
                _wasSelected = _isSelected
                _isSelected = false
            }
        }
        if (string.size == 2) {
            if (string[0].equals(MESSAGE.CURRENT_POSITION.toString(), ignoreCase = true)) {
                _currentPosition = _json.fromJson(Vector2::class.java, string[1])
            } else if (string[0].equals(MESSAGE.INIT_START_POSITION.toString(), ignoreCase = true)) {
                _currentPosition = _json.fromJson(Vector2::class.java, string[1])
            } else if (string[0].equals(MESSAGE.CURRENT_STATE.toString(), ignoreCase = true)) {
                _currentState = _json.fromJson(Entity.State::class.java, string[1])
            } else if (string[0].equals(MESSAGE.CURRENT_DIRECTION.toString(), ignoreCase = true)) {
                _currentDirection = _json.fromJson(Entity.Direction::class.java, string[1])
            } else if (string[0].equals(MESSAGE.LOAD_ANIMATIONS.toString(), ignoreCase = true)) {
                val entityConfig = _json.fromJson(EntityConfig::class.java, string[1])
                val animationConfigs = entityConfig.animationConfig
                for (animationConfig in animationConfigs) {
                    val textureNames = animationConfig.texturePaths
                    val points = animationConfig.gridPoints
                    val animationType = animationConfig.animationType
                    val frameDuration = animationConfig.frameDuration
                    var animation: Animation<TextureRegion>? = null
                    if (textureNames.size == 1) {
                        animation = loadAnimation(textureNames[0]!!, points, frameDuration)
                    } else if (textureNames.size == 2) {
                        animation = loadAnimation(textureNames[0]!!, textureNames[1]!!, points, frameDuration)
                    }
                    _animations[animationType] = animation
                }
            }
        }
    }

    override fun update(entity: Entity, mapManager: MapManager, batch: Batch, delta: Float) {
        updateAnimations(delta)
        if (_isSelected) {
            drawSelected(entity, mapManager)
            mapManager.currentSelectedMapEntity = entity
            if (_sentShowConversationMessage == false) {
                notify(_json.toJson(entity.entityConfig), ComponentObserver.ComponentEvent.SHOW_CONVERSATION)
                _sentShowConversationMessage = true
                _sentHideCoversationMessage = false
            }
        } else {
            if (_sentHideCoversationMessage == false) {
                notify(_json.toJson(entity.entityConfig), ComponentObserver.ComponentEvent.HIDE_CONVERSATION)
                _sentHideCoversationMessage = true
                _sentShowConversationMessage = false
            }
        }
        batch.begin()
        batch.draw(_currentFrame, _currentPosition.x, _currentPosition.y, 1f, 1f)
        batch.end()

        //Used to graphically debug boundingboxes
        /*
        Rectangle rect = entity.getCurrentBoundingBox();
        Camera camera = mapMgr.getCamera();
        _shapeRenderer.setProjectionMatrix(camera.combined);
        _shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        _shapeRenderer.setColor(Color.BLACK);
        _shapeRenderer.rect(rect.getX() * Map.UNIT_SCALE, rect.getY() * Map.UNIT_SCALE, rect.getWidth() * Map.UNIT_SCALE, rect.getHeight() * Map.UNIT_SCALE);
        _shapeRenderer.end();
        */
    }

    private fun drawSelected(entity: Entity, mapMgr: MapManager) {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        val camera = mapMgr.camera
        val rect = entity.currentBoundingBox
        _shapeRenderer.projectionMatrix = camera!!.combined
        _shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        _shapeRenderer.setColor(0.0f, 1.0f, 1.0f, 0.5f)
        val width: Float = rect!!.getWidth() * Map.Companion.UNIT_SCALE * 2f
        val height: Float = rect.getHeight() * Map.Companion.UNIT_SCALE / 2f
        val x: Float = rect.x * Map.Companion.UNIT_SCALE - width / 4
        val y: Float = rect.y * Map.Companion.UNIT_SCALE - height / 2
        _shapeRenderer.ellipse(x, y, width, height)
        _shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    override fun dispose() {}

    companion object {
        private val TAG = NPCGraphicsComponent::class.java.simpleName
    }
}