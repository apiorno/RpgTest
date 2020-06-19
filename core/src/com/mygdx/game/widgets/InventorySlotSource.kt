package com.mygdx.game.widgets

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload

class InventorySlotSource(var sourceSlot: InventorySlot, private val _dragAndDrop: DragAndDrop?) : DragAndDrop.Source(sourceSlot.topInventoryItem) {
    override fun dragStart(event: InputEvent, x: Float, y: Float, pointer: Int): Payload? {
        val payload = Payload()
        val actor = actor ?: return null
        val source = actor.parent as InventorySlot?
        if (source == null) {
            return null
        } else {
            sourceSlot = source
        }
        sourceSlot.decrementItemCount(true)
        payload.dragActor = getActor()
        _dragAndDrop!!.setDragActorPosition(-x, -y + getActor().height)
        return payload
    }

    override fun dragStop(event: InputEvent, x: Float, y: Float, pointer: Int, payload: Payload, target: DragAndDrop.Target) {
        if (target == null) {
            sourceSlot.add(payload.dragActor)
        }
    }

}