package com.mygdx.game.maps

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.maps.tiled.TiledMap
import com.mygdx.game.Utility
import com.mygdx.game.audio.AudioObserver

class TopWorldMapContentController : MapContentController(){

    private  val mapPath = "maps/topworld.tmx"
    override val mapType = MapFactory.MapType.TOP_WORLD
    override fun unloadMusic() {
        notify(AudioObserver.AudioCommand.MUSIC_STOP, AudioObserver.AudioTypeEvent.MUSIC_TOPWORLD)
    }

    override fun loadMusic() {
        notify(AudioObserver.AudioCommand.MUSIC_LOAD, AudioObserver.AudioTypeEvent.MUSIC_TOPWORLD)
        notify(AudioObserver.AudioCommand.MUSIC_PLAY_LOOP, AudioObserver.AudioTypeEvent.MUSIC_TOPWORLD)
    }

    override fun loadMap(): TiledMap? {
        Utility.loadMapAsset(mapPath)
        if (!Utility.isAssetLoaded(mapPath)) {
            Gdx.app.debug(TAG, "Map not loaded")
        }
        return Utility.getMapAsset(mapPath)
    }

    companion object {
        private val TAG = TopWorldMapContentController::class.java.simpleName
    }
}