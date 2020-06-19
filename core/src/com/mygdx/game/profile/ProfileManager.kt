package com.mygdx.game.profile

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Base64Coder
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.ObjectMap
import com.mygdx.game.profile.ProfileObserver.ProfileEvent
import java.util.*

class ProfileManager private constructor() : ProfileSubject() {
    private val _json: Json
    private var _profiles: Hashtable<String, FileHandle?>? = null
    private var _profileProperties = ObjectMap<String, Any>()
    private var _profileName: String
    var isNewProfile = false

    val profileList: Array<String>
        get() {
            val profiles = Array<String>()
            val e = _profiles!!.keys()
            while (e.hasMoreElements()) {
                profiles.add(e.nextElement())
            }
            return profiles
        }

    fun getProfileFile(profile: String): FileHandle? {
        return if (!doesProfileExist(profile)) {
            null
        } else _profiles!![profile]
    }

    fun storeAllProfiles() {
        if (Gdx.files.isLocalStorageAvailable) {
            val files = Gdx.files.local(".").list(SAVEGAME_SUFFIX)
            for (file in files) {
                _profiles!![file.nameWithoutExtension()] = file
            }
        } else {
            //TODO: try external directory here
            return
        }
    }

    fun doesProfileExist(profileName: String): Boolean {
        return _profiles!!.containsKey(profileName)
    }

    fun writeProfileToStorage(profileName: String, fileData: String?, overwrite: Boolean) {
        val fullFilename = profileName + SAVEGAME_SUFFIX
        val localFileExists = Gdx.files.local(fullFilename).exists()

        //If we cannot overwrite and the file exists, exit
        if (localFileExists && !overwrite) {
            return
        }
        var file: FileHandle? = null
        if (Gdx.files.isLocalStorageAvailable) {
            file = Gdx.files.local(fullFilename)
            val encodedString = Base64Coder.encodeString(fileData)
            file.writeString(encodedString, !overwrite)
        }
        _profiles!![profileName] = file
    }

    fun setProperty(key: String, `object`: Any) {
        _profileProperties.put(key, `object`)
    }

    fun <T : Any?> getProperty(key: String, type: Class<T>?): T? {
        var property: T? = null
        if (!_profileProperties.containsKey(key)) {
            return property
        }
        property = _profileProperties[key] as T
        return property
    }

    fun saveProfile() {
        notify(this, ProfileEvent.SAVING_PROFILE)
        val text = _json.prettyPrint(_json.toJson(_profileProperties))
        writeProfileToStorage(_profileName, text, true)
    }

    fun loadProfile() {
        if (isNewProfile) {
            notify(this, ProfileEvent.CLEAR_CURRENT_PROFILE)
            saveProfile()
        }
        val fullProfileFileName = _profileName + SAVEGAME_SUFFIX
        val doesProfileFileExist = Gdx.files.local(fullProfileFileName).exists()
        if (!doesProfileFileExist) {
            //System.out.println("File doesn't exist!");
            return
        }
        val encodedFile = _profiles!![_profileName]
        val s = encodedFile!!.readString()
        val decodedFile = Base64Coder.decodeString(s)
        _profileProperties = _json.fromJson(ObjectMap::class.java, decodedFile) as ObjectMap<String, Any>
        notify(this, ProfileEvent.PROFILE_LOADED)
        isNewProfile = false
    }

    fun setCurrentProfile(profileName: String) {
        _profileName = if (doesProfileExist(profileName)) {
            profileName
        } else {
            DEFAULT_PROFILE
        }
    }

    companion object {
        private val TAG = ProfileManager::class.java.simpleName
        private var _profileManager: ProfileManager? = null
        private const val SAVEGAME_SUFFIX = ".sav"
        const val DEFAULT_PROFILE = "default"
        @JvmStatic
        val instance: ProfileManager
            get() {
                if (_profileManager == null) {
                    _profileManager = ProfileManager()
                }
                return _profileManager!!
            }
    }

    init {
        _json = Json()
        _profiles = Hashtable()
        _profiles!!.clear()
        _profileName = DEFAULT_PROFILE
        storeAllProfiles()
    }
}