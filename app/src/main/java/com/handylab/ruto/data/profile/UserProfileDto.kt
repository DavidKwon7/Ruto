package com.handylab.ruto.data.profile

import com.handylab.ruto.domain.profile.DEFAULT_NICKNAME
import com.handylab.ruto.domain.profile.UserProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    @SerialName("user_id") val userId: String,
    val nickname: String,
    @SerialName("avatar_path") val avatarPath: String? = null,
    @SerialName("avatar_version") val avatarVersion: Int = 0,
)

fun UserProfileDto.toDomain(avatarUrl: String?): UserProfile =
    UserProfile(
        nickname = nickname.ifBlank { DEFAULT_NICKNAME },
        avatarUrl = avatarUrl,
        avatarVersion = avatarVersion
    )
