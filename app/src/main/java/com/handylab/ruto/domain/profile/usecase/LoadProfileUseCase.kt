package com.handylab.ruto.domain.profile.usecase

import com.handylab.ruto.domain.profile.ProfileRepository
import com.handylab.ruto.domain.profile.UserProfile
import javax.inject.Inject

class LoadProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(): UserProfile = repository.loadProfile()
}
