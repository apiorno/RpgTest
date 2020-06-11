package com.mygdx.game.maps

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.maps.tiled.TiledMap
import com.mygdx.game.Utility
import com.mygdx.game.audio.AudioObserver

class TownMapContentController : MapContentController() {

    private  val mapPath = "maps/town.tmx"
    override val mapType = MapFactory.MapType.TOWN
    override fun unloadMusic() {
        notify(AudioObserver.AudioCommand.MUSIC_STOP, AudioObserver.AudioTypeEvent.MUSIC_TOWN)
    }

    override fun loadMusic() {
        notify(AudioObserver.AudioCommand.MUSIC_LOAD, AudioObserver.AudioTypeEvent.MUSIC_TOWN)
        notify(AudioObserver.AudioCommand.MUSIC_PLAY_LOOP, AudioObserver.AudioTypeEvent.MUSIC_TOWN)
    }
    override fun loadMap(): TiledMap? {
        Utility.loadMapAsset(mapPath)
        if (!Utility.isAssetLoaded(mapPath)) {
            Gdx.app.debug(TAG, "Map not loaded")
        }
        return Utility.getMapAsset(mapPath)
    }

    companion object {
        private val TAG = TownMapContentController::class.java.simpleName
    }
}