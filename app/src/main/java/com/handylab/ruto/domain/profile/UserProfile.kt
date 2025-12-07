package com.handylab.ruto.domain.profile

const val DEFAULT_NICKNAME = "미설정 닉네임"

data class UserProfile(
    val nickname: String = DEFAULT_NICKNAME,
    val avatarUrl: String? = null, // Coil에 바로 넘길 URL
)
