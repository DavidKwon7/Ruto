package com.handylab.ruto.domain.profile

import android.net.Uri

interface ProfileRepository {
    suspend fun loadProfile(): UserProfile
    suspend fun updateNickname(newNickname: String): UserProfile
    suspend fun updateAvatar(avatarUri: Uri): UserProfile
}
