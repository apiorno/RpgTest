package com.mygdx.game.widgets

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop

class InventorySlotTarget(var targetSlot: InventorySlot) : DragAndDrop.Target(targetSlot) {
    override fun drag(source: DragAndDrop.Source, payload: DragAndDrop.Payload, x: Float, y: Float, pointer: Int): Boolean {
        return true
    }

    override fun reset(source: DragAndDrop.Source, payload: DragAndDrop.Payload) {}
    override fun drop(source: DragAndDrop.Source, payload: DragAndDrop.Payload, x: Float, y: Float, pointer: Int) {
        val sourceActor = payload.dragActor as InventoryItem
        val targetActor = targetSlot.topInventoryItem
        val sourceSlot = (source as InventorySlotSource).sourceSlot
        if (sourceActor == null) {
            return
        }

        //First, does the slot accept the source item type?
        if (!targetSlot.doesAcceptItemUseType(sourceActor.itemUseType)) {
            //Put item back where it came from, slot doesn't accept item
            sourceSlot.add(sourceActor)
            return
        }
        if (!targetSlot.hasItem()) {
            targetSlot.add(sourceActor)
        } else {
            //If the same item and stackable, add
            if (sourceActor.isSameItemType(targetActor!!) && sourceActor.isStackable) {
                targetSlot.add(sourceActor)
            } else {
                //If they aren't the same items or the items aren't stackable, then swap
                InventorySlot.Companion.swapSlots(sourceSlot, targetSlot, sourceActor)
            }
        }
    }

}