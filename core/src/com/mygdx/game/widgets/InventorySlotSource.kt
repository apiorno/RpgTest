package com.mygdx.game.widgets

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop

class InventorySlotSource(var sourceSlot: InventorySlot, private val dragAndDrop: DragAndDrop?) : DragAndDrop.Source(sourceSlot.topInventoryItem) {
    override fun dragStart(event: InputEvent, x: Float, y: Float, pointer: Int): DragAndDrop.Payload? {
        val payload = DragAndDrop.Payload()
        val actor = actor ?: return null
        val source = actor.parent as InventorySlot
        if (source == null)  return null else  sourceSlot = source

        sourceSlot.decrementItemCount(true)
        payload.dragActor = getActor()
        dragAndDrop!!.setDragActorPosition(-x, -y + getActor().height)
        return payload
    }

    override fun dragStop(event: InputEvent, x: Float, y: Float, pointer: Int, payload: DragAndDrop.Payload, target: DragAndDrop.Target) {
        if (target == null) sourceSlot.add(payload.dragActor)

    }
}

