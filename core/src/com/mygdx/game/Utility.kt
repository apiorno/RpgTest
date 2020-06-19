package com.mygdx.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.MusicLoader
import com.badlogic.gdx.assets.loaders.SoundLoader
import com.badlogic.gdx.assets.loaders.TextureLoader
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.scenes.scene2d.ui.Skin

object Utility {
    val _assetManager = AssetManager()
    private val TAG = Utility::class.java.simpleName
    private val _filePathResolver = InternalFileHandleResolver()
    private const val STATUSUI_TEXTURE_ATLAS_PATH = "skins/statusui.atlas"
    private const val STATUSUI_SKIN_PATH = "skins/statusui.json"
    private const val ITEMS_TEXTURE_ATLAS_PATH = "skins/items.atlas"
    private const val ITEMS_SKIN_PATH = "skins/items.json"
    @kotlin.jvm.JvmField
    var STATUSUI_TEXTUREATLAS = TextureAtlas(STATUSUI_TEXTURE_ATLAS_PATH)
    @kotlin.jvm.JvmField
    var ITEMS_TEXTUREATLAS = TextureAtlas(ITEMS_TEXTURE_ATLAS_PATH)
    @kotlin.jvm.JvmField
    var STATUSUI_SKIN = Skin(Gdx.files.internal(STATUSUI_SKIN_PATH), STATUSUI_TEXTUREATLAS)
    fun unloadAsset(assetFilenamePath: String) {
        // once the asset manager is done loading
        if (_assetManager.isLoaded(assetFilenamePath)) {
            _assetManager.unload(assetFilenamePath)
        } else {
            Gdx.app.debug(TAG, "Asset is not loaded; Nothing to unload: $assetFilenamePath")
        }
    }

    fun loadCompleted(): Float {
        return _assetManager.progress
    }

    fun numberAssetsQueued(): Int {
        return _assetManager.queuedAssets
    }

    fun updateAssetLoading(): Boolean {
        return _assetManager.update()
    }

    @kotlin.jvm.JvmStatic
    fun isAssetLoaded(fileName: String?): Boolean {
        return _assetManager.isLoaded(fileName)
    }

    fun loadMapAsset(mapFilenamePath: String?) {
        if (mapFilenamePath == null || mapFilenamePath.isEmpty()) {
            return
        }
        if (_assetManager.isLoaded(mapFilenamePath)) {
            return
        }

        //load asset
        if (_filePathResolver.resolve(mapFilenamePath).exists()) {
            _assetManager.setLoader(TiledMap::class.java, TmxMapLoader(_filePathResolver))
            _assetManager.load(mapFilenamePath, TiledMap::class.java)
            //Until we add loading screen, just block until we load the map
            _assetManager.finishLoadingAsset(mapFilenamePath) as TiledMap
            Gdx.app.debug(TAG, "Map loaded!: $mapFilenamePath")
        } else {
            Gdx.app.debug(TAG, "Map doesn't exist!: $mapFilenamePath")
        }
    }

    fun getMapAsset(mapFilenamePath: String?): TiledMap? {
        var map: TiledMap? = null

        // once the asset manager is done loading
        if (_assetManager.isLoaded(mapFilenamePath)) {
            map = _assetManager.get(mapFilenamePath, TiledMap::class.java)
        } else {
            Gdx.app.debug(TAG, "Map is not loaded: $mapFilenamePath")
        }
        return map
    }

    @kotlin.jvm.JvmStatic
    fun loadSoundAsset(soundFilenamePath: String?) {
        if (soundFilenamePath == null || soundFilenamePath.isEmpty()) {
            return
        }
        if (_assetManager.isLoaded(soundFilenamePath)) {
            return
        }

        //load asset
        if (_filePathResolver.resolve(soundFilenamePath).exists()) {
            _assetManager.setLoader(Sound::class.java, SoundLoader(_filePathResolver))
            _assetManager.load(soundFilenamePath, Sound::class.java)
            //Until we add loading screen, just block until we load the map
            _assetManager.finishLoadingAsset(soundFilenamePath) as Sound
            Gdx.app.debug(TAG, "Sound loaded!: $soundFilenamePath")
        } else {
            Gdx.app.debug(TAG, "Sound doesn't exist!: $soundFilenamePath")
        }
    }

    @kotlin.jvm.JvmStatic
    fun getSoundAsset(soundFilenamePath: String): Sound? {
        var sound: Sound? = null

        // once the asset manager is done loading
        if (_assetManager.isLoaded(soundFilenamePath)) {
            sound = _assetManager.get(soundFilenamePath, Sound::class.java)
        } else {
            Gdx.app.debug(TAG, "Sound is not loaded: $soundFilenamePath")
        }
        return sound
    }

    @kotlin.jvm.JvmStatic
    fun loadMusicAsset(musicFilenamePath: String?) {
        if (musicFilenamePath == null || musicFilenamePath.isEmpty()) {
            return
        }
        if (_assetManager.isLoaded(musicFilenamePath)) {
            return
        }

        //load asset
        if (_filePathResolver.resolve(musicFilenamePath).exists()) {
            _assetManager.setLoader(Music::class.java, MusicLoader(_filePathResolver))
            _assetManager.load(musicFilenamePath, Music::class.java)
            //Until we add loading screen, just block until we load the map
            _assetManager.finishLoadingAsset(musicFilenamePath) as Music
            Gdx.app.debug(TAG, "Music loaded!: $musicFilenamePath")
        } else {
            Gdx.app.debug(TAG, "Music doesn't exist!: $musicFilenamePath")
        }
    }

    @kotlin.jvm.JvmStatic
    fun getMusicAsset(musicFilenamePath: String): Music? {
        var music: Music? = null

        // once the asset manager is done loading
        if (_assetManager.isLoaded(musicFilenamePath)) {
            music = _assetManager.get(musicFilenamePath, Music::class.java)
        } else {
            Gdx.app.debug(TAG, "Music is not loaded: $musicFilenamePath")
        }
        return music
    }

    fun loadTextureAsset(textureFilenamePath: String?) {
        if (textureFilenamePath == null || textureFilenamePath.isEmpty()) {
            return
        }
        if (_assetManager.isLoaded(textureFilenamePath)) {
            return
        }

        //load asset
        if (_filePathResolver.resolve(textureFilenamePath).exists()) {
            _assetManager.setLoader(Texture::class.java, TextureLoader(_filePathResolver))
            _assetManager.load(textureFilenamePath, Texture::class.java)
            //Until we add loading screen, just block until we load the map
            _assetManager.finishLoadingAsset(textureFilenamePath)
        } else {
            Gdx.app.debug(TAG, "Texture doesn't exist!: $textureFilenamePath")
        }
    }

    fun getTextureAsset(textureFilenamePath: String): Texture? {
        var texture: Texture? = null

        // once the asset manager is done loading
        if (_assetManager.isLoaded(textureFilenamePath)) {
            texture = _assetManager.get(textureFilenamePath, Texture::class.java)
        } else {
            Gdx.app.debug(TAG, "Texture is not loaded: $textureFilenamePath")
        }
        return texture
    }
}