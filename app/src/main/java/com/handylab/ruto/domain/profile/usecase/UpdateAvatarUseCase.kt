package com.handylab.ruto.domain.profile.usecase

import android.net.Uri
import com.handylab.ruto.domain.profile.ProfileRepository
import com.handylab.ruto.domain.profile.UserProfile
import javax.inject.Inject

class UpdateAvatarUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(avatarUri: Uri): UserProfile =
        repository.updateAvatar(avatarUri)
}
