package com.handylab.ruto.data.profile

import com.handylab.ruto.domain.profile.DEFAULT_NICKNAME
import com.handylab.ruto.domain.profile.UserProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileEntity(
    @SerialName("user_id") val userId: String,
    val nickname: String,
    @SerialName("avatar_path") val avatarPath: String? = null,
)

fun UserProfileEntity.toDomain(avatarUrl: String?): UserProfile =
    UserProfile(
        nickname = nickname.ifBlank { DEFAULT_NICKNAME },
        avatarUrl = avatarUrl
    )
