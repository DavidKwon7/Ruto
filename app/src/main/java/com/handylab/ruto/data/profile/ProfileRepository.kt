package com.handylab.ruto.data.profile

import android.content.Context
import android.net.Uri
import com.handylab.ruto.domain.profile.DEFAULT_NICKNAME
import com.handylab.ruto.domain.profile.UserProfile
import com.handylab.ruto.util.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class ProfileRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val logger: AppLogger,
    @ApplicationContext private val context: Context,
) {

    private suspend fun currentUserId(): String {
        val session = supabase.auth.currentSessionOrNull()
            ?: error("로그인이 필요합니다.")
        return session.user?.id ?: error("유효하지 않은 세션입니다.")
    }

    private fun avatarPath(userId: String): String =
        "$userId/avatar.jpg"

    private suspend fun resolveAvatarUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null

        return try {
            supabase.storage.from("avatars")
                .createSignedUrl(
                    path = path,
                    expiresIn = 60.minutes,
                )
        } catch (e: Exception) {
            logger.w("Profile", "avatar not found: $path", e)
            null
        }
    }

    suspend fun loadProfile(): UserProfile {
        val userId = currentUserId()

        val rows = supabase.postgrest["app_user_profiles"]
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<UserProfileEntity>()

        val row = rows.firstOrNull()
        if (row == null) {
            // 아직 프로필 레코드가 없을 때: 기본값
            return UserProfile(
                nickname = DEFAULT_NICKNAME,
                avatarUrl = null
            )
        }

        val avatarUrl = resolveAvatarUrl(row.avatarPath)
        return row.toDomain(avatarUrl)
    }

    suspend fun updateNickname(newNickname: String): UserProfile {
        val userId = currentUserId()
        val nickname = newNickname.ifBlank { DEFAULT_NICKNAME }

        val existingRows = supabase.postgrest["app_user_profiles"]
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<UserProfileEntity>()

        val existing = existingRows.firstOrNull()

        val upsertRow = UserProfileEntity(
            userId = userId,
            nickname = nickname,
            avatarPath = existing?.avatarPath
        )

        val row = supabase.postgrest["app_user_profiles"]
            .upsert(upsertRow) {
                select()
            }
            .decodeSingle<UserProfileEntity>()

        val avatarUrl = resolveAvatarUrl(row.avatarPath)
        return row.toDomain(avatarUrl)
    }


    suspend fun updateAvatar(avatarUri: Uri): UserProfile {
        val userId = currentUserId()
        val path = avatarPath(userId)

        val bytes = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(avatarUri)?.use { input ->
                input.readBytes()
            } ?: error("이미지 파일을 읽을 수 없습니다.")
        }

        val bucket = supabase.storage.from("avatars")

        bucket.upload(
            path = path,
            data = bytes
        ) {
            upsert = true
        }

        val existingRows = supabase.postgrest["app_user_profiles"]
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<UserProfileEntity>()

        val existing = existingRows.firstOrNull()
        val nickname = existing?.nickname ?: DEFAULT_NICKNAME

        val upsertRow = UserProfileEntity(
            userId = userId,
            nickname = nickname,
            avatarPath = path
        )

        val row = supabase.postgrest["app_user_profiles"]
            .upsert(upsertRow) {
                select()
            }
            .decodeSingle<UserProfileEntity>()

        val avatarUrl = resolveAvatarUrl(row.avatarPath)
        return row.toDomain(avatarUrl)
    }
}
