package com.mygdx.game.profile

interface ProfileObserver {
    enum class ProfileEvent {
        PROFILE_LOADED, SAVING_PROFILE, CLEAR_CURRENT_PROFILE
    }

    fun onNotify(profileManager: ProfileManager, event: ProfileEvent)
}