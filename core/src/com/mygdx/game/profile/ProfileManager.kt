package com.mygdx.game.profile

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Base64Coder
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.ObjectMap
import java.util.*

class ProfileManager : ProfileSubject() {
    private val json: Json = Json()
    private var profiles: Hashtable<String, FileHandle?>? = null
    private var profileProperties = ObjectMap<String, Any>()
    private var profileName: String
    var isNewProfile = false

    init {
        profiles = Hashtable()
        profiles!!.clear()
        profileName = DEFAULT_PROFILE
        storeAllProfiles()
    }

    val profileList: Array<String>
        get() {
            val profilesList = Array<String>()
            val e = profiles!!.keys()
            while (e.hasMoreElements()) {
                profilesList.add(e.nextElement())
            }
            return profilesList
        }

    fun getProfileFile(profile: String): FileHandle? {
        return if (!doesProfileExist(profile)) {
            null
        } else profiles!![profile]
    }

    fun storeAllProfiles() {
        if (Gdx.files.isLocalStorageAvailable) {
            val files = Gdx.files.local(".").list(SAVEGAME_SUFFIX)
            for (file in files) {
                profiles!![file.nameWithoutExtension()] = file
            }
        } else {
            //TODO: try external directory here
            return
        }
    }

    fun doesProfileExist(profileName: String): Boolean {
        return profiles!!.containsKey(profileName)
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
        profiles!![profileName] = file
    }

    fun setProperty(key: String, `object`: Any) {
        profileProperties.put(key, `object`)
    }

    fun <T : Any?> getProperty(key: String, type: Class<T>?): T? {
        var property: T? = null
        if (!profileProperties.containsKey(key)) {
            return property
        }
        property = profileProperties[key] as T
        return property
    }

    fun saveProfile() {
        notify(this, ProfileObserver.ProfileEvent.SAVING_PROFILE)
        val text = json.prettyPrint(json.toJson(profileProperties))
        writeProfileToStorage(profileName, text, true)
    }

    fun loadProfile() {
        if (isNewProfile) {
            notify(this, ProfileObserver.ProfileEvent.CLEAR_CURRENT_PROFILE)
            saveProfile()
        }
        val fullProfileFileName = profileName + SAVEGAME_SUFFIX
        val doesProfileFileExist = Gdx.files.local(fullProfileFileName).exists()
        if (!doesProfileFileExist) {
            //System.out.println("File doesn't exist!");
            return
        }
        val encodedFile = profiles!![profileName]
        val s = encodedFile!!.readString()
        val decodedFile = Base64Coder.decodeString(s)
        profileProperties = json.fromJson(ObjectMap::class.java, decodedFile) as ObjectMap<String, Any>
        notify(this, ProfileObserver.ProfileEvent.PROFILE_LOADED)
        isNewProfile = false
    }

    fun setCurrentProfile(profileName: String) {
        this.profileName =if (doesProfileExist(profileName)) profileName else DEFAULT_PROFILE
    }

    companion object {
        private val TAG = ProfileManager::class.java.simpleName
        private var uniqueInstance: ProfileManager? = null
        private const val SAVEGAME_SUFFIX = ".sav"
        const val DEFAULT_PROFILE = "default"
        @JvmStatic
        val instance: ProfileManager
            get() {
                if (uniqueInstance == null) {
                    uniqueInstance = ProfileManager()
                }
                return uniqueInstance!!
            }
    }


}