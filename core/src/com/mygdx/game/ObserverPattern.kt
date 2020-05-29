package com.mygdx.game

interface IObserver{
    fun receiveNotification(event : Event)
}

interface IObservable{
    val observers : ArrayList<IObserver>
    fun add(observer : IObserver){
        observers.add(observer)
    }
    fun remove(observer : IObserver){
        observers.remove(observer)
    }
    fun notifyObservers(event: Event){
        observers.forEach { it.receiveNotification(event) }
    }
}

 enum class EventType {
    INPUT_PRESSED, INPUT_RELEASED
}
class Event (val eventType: EventType,val value: PlayerInputProcessor.Key)