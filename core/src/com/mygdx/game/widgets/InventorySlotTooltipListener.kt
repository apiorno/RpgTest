package com.mygdx.game.widgets

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener

class InventorySlotTooltipListener(private val toolTip: InventorySlotTooltip) : InputListener() {
    private var isInside = false
    private val currentCoords: Vector2 = Vector2(0F, 0F)
    private val offset: Vector2 = Vector2(20F, 10F)
    override fun mouseMoved(event: InputEvent, x: Float, y: Float): Boolean {
        val inventorySlot = event.listenerActor as InventorySlot
        if (isInside) {
            currentCoords[x] = y
            inventorySlot.localToStageCoordinates(currentCoords)
            toolTip.setPosition(currentCoords.x + offset.x, currentCoords.y + offset.y)
        }
        return false
    }

    override fun touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int) {
        val inventorySlot = event.listenerActor as InventorySlot
        toolTip.setVisible(inventorySlot, false)
    }

    override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return true
    }

    override fun enter(event: InputEvent, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
        val inventorySlot = event.listenerActor as InventorySlot
        isInside = true
        currentCoords[x] = y
        inventorySlot.localToStageCoordinates(currentCoords)
        toolTip.updateDescription(inventorySlot)
        toolTip.setPosition(currentCoords.x + offset.x, currentCoords.y + offset.y)
        toolTip.toFront()
        toolTip.setVisible(inventorySlot, true)
    }

    override fun exit(event: InputEvent, x: Float, y: Float, pointer: Int, toActor: Actor?) {
        val inventorySlot = event.listenerActor as InventorySlot
        toolTip.setVisible(inventorySlot, false)
        isInside = false
        currentCoords[x] = y
        inventorySlot.localToStageCoordinates(currentCoords)
    }

}