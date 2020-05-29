package com.mygdx.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetDescriptor
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

    val assetManager = AssetManager()
    private val TAG = Utility::class.java.simpleName
    private val filePathResolver = InternalFileHandleResolver()
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
        if (assetManager.isLoaded(assetFilenamePath)) {
            assetManager.unload(assetFilenamePath)
        } else {
            Gdx.app.debug(TAG, "Asset is not loaded; Nothing to unload: $assetFilenamePath")
        }
    }

    fun loadCompleted(): Float {
        return assetManager.progress
    }

    fun numberAssetsQueued(): Int {
        return assetManager.queuedAssets
    }

    fun updateAssetLoading(): Boolean {
        return assetManager.update()
    }

    @kotlin.jvm.JvmStatic
    fun isAssetLoaded(fileName: String?): Boolean {
        return assetManager.isLoaded(fileName)
    }

    fun loadMapAsset(mapFilenamePath: String?) {
        if (mapFilenamePath == null || mapFilenamePath.isEmpty()) {
            return
        }
        if (assetManager.isLoaded(mapFilenamePath)) {
            return
        }

        //load asset
        if (filePathResolver.resolve(mapFilenamePath).exists()) {
            assetManager.setLoader(TiledMap::class.java, TmxMapLoader(filePathResolver))
            assetManager.load(mapFilenamePath, TiledMap::class.java)
            //Until we add loading screen, just block until we load the map
            assetManager.finishLoadingAsset(mapFilenamePath ) as TiledMap
            Gdx.app.debug(TAG, "Map loaded!: $mapFilenamePath")
        } else {
            Gdx.app.debug(TAG, "Map doesn't exist!: $mapFilenamePath")
        }
    }

    fun getMapAsset(mapFilenamePath: String?): TiledMap? {
        var map: TiledMap? = null

        // once the asset manager is done loading
        if (assetManager.isLoaded(mapFilenamePath)) {
            map = assetManager.get(mapFilenamePath, TiledMap::class.java)
        } else {
            Gdx.app.debug(TAG, "Map is not loaded: $mapFilenamePath")
        }
        return map
    }

    @kotlin.jvm.JvmStatic
    fun loadSoundAsset(soundFilenamePath: String?) {
        if (soundFilenamePath == null || soundFilenamePath.isEmpty() || assetManager.isLoaded(soundFilenamePath)) {
            return

        }
        //load asset

        if (filePathResolver.resolve(soundFilenamePath).exists()) {
            assetManager.setLoader(Sound::class.java, SoundLoader(filePathResolver))
            assetManager.load(soundFilenamePath, Sound::class.java)
            //Until we add loading screen, just block until we load the map
            assetManager.finishLoadingAsset(soundFilenamePath) as Sound
            Gdx.app.debug(TAG, "Sound loaded!: $soundFilenamePath")
        } else {
            Gdx.app.debug(TAG, "Sound doesn't exist!: $soundFilenamePath")
        }
    }

    @kotlin.jvm.JvmStatic
    fun getSoundAsset(soundFilenamePath: String): Sound? {
        var sound: Sound? = null

        // once the asset manager is done loading
        if (assetManager.isLoaded(soundFilenamePath)) {
            sound = assetManager.get(soundFilenamePath, Sound::class.java)
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
        if (assetManager.isLoaded(musicFilenamePath)) {
            return
        }

        //load asset
        if (filePathResolver.resolve(musicFilenamePath).exists()) {
            assetManager.setLoader(Music::class.java, MusicLoader(filePathResolver))
            assetManager.load(musicFilenamePath, Music::class.java)
            //Until we add loading screen, just block until we load the map
            assetManager.finishLoadingAsset(musicFilenamePath) as Music
            Gdx.app.debug(TAG, "Music loaded!: $musicFilenamePath")
        } else {
            Gdx.app.debug(TAG, "Music doesn't exist!: $musicFilenamePath")
        }
    }

    @kotlin.jvm.JvmStatic
    fun getMusicAsset(musicFilenamePath: String): Music? {
        var music: Music? = null

        // once the asset manager is done loading
        if (assetManager.isLoaded(musicFilenamePath)) {
            music = assetManager.get(musicFilenamePath, Music::class.java)
        } else {
            Gdx.app.debug(TAG, "Music is not loaded: $musicFilenamePath")
        }
        return music
    }

    fun loadTextureAsset(textureFilenamePath: String?) {
        if (textureFilenamePath == null || textureFilenamePath.isEmpty()) {
            return
        }
        if (assetManager.isLoaded(textureFilenamePath)) {
            return
        }

        //load asset
        if (filePathResolver.resolve(textureFilenamePath).exists()) {
            assetManager.setLoader(Texture::class.java, TextureLoader(filePathResolver))
            assetManager.load(textureFilenamePath, Texture::class.java)
            //Until we add loading screen, just block until we load the map
            assetManager.finishLoadingAsset(textureFilenamePath)
        } else {
            Gdx.app.debug(TAG, "Texture doesn't exist!: $textureFilenamePath")
        }
    }

    fun getTextureAsset(textureFilenamePath: String): Texture? {
        var texture: Texture? = null

        // once the asset manager is done loading
        if (assetManager.isLoaded(textureFilenamePath)) {
            texture = assetManager.get(textureFilenamePath, Texture::class.java)
        } else {
            Gdx.app.debug(TAG, "Texture is not loaded: $textureFilenamePath")
        }
        return texture
    }

}