package com.mygdx.game.ecs

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.mygdx.game.ecs.Component.MESSAGE
import com.mygdx.game.maps.MapManager

class PlayerGraphicsComponent : GraphicsComponent() {
    protected var _previousPosition: Vector2
    override fun receiveMessage(message: String) {
        //Gdx.app.debug(TAG, "Got message " + message);
        val string: Array<String> = message.split(Component.Companion.MESSAGE_TOKEN).toTypedArray()
        if (string.size == 0) return

        //Specifically for messages with 1 object payload
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

        //Player has moved
        if (_previousPosition.x != _currentPosition.x ||
                _previousPosition.y != _currentPosition.y) {
            notify("", ComponentObserver.ComponentEvent.PLAYER_HAS_MOVED)
            _previousPosition = _currentPosition.cpy()
        }
        val camera = mapManager.camera
        camera!!.position[_currentPosition.x, _currentPosition.y] = 0f
        camera.update()
        batch.begin()
        batch.draw(_currentFrame, _currentPosition.x, _currentPosition.y, 1f, 1f)
        batch.end()

        //Used to graphically debug boundingboxes
        /*
        Rectangle rect = entity.getCurrentBoundingBox();
        _shapeRenderer.setProjectionMatrix(camera.combined);
        _shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        _shapeRenderer.setColor(Color.RED);
        _shapeRenderer.rect(rect.getX() * Map.UNIT_SCALE , rect.getY() * Map.UNIT_SCALE, rect.getWidth() * Map.UNIT_SCALE, rect.getHeight()*Map.UNIT_SCALE);
        _shapeRenderer.end();
        */
    }

    override fun dispose() {}

    companion object {
        private val TAG = PlayerGraphicsComponent::class.java.simpleName
    }

    init {
        _previousPosition = Vector2(0F, 0F)
    }
}