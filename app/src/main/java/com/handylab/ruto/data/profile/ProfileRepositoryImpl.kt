package com.handylab.ruto.data.profile

import android.content.Context
import android.net.Uri
import com.handylab.ruto.domain.profile.DEFAULT_NICKNAME
import com.handylab.ruto.domain.profile.ProfileRepository
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

class ProfileRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val logger: AppLogger,
    @param:ApplicationContext private val context: Context,
) : ProfileRepository {

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

    override suspend fun loadProfile(): UserProfile {
        val userId = currentUserId()

        val rows = supabase.postgrest["app_user_profiles"]
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<UserProfileDto>()

        val row = rows.firstOrNull()
        if (row == null) {
            return UserProfile(
                nickname = DEFAULT_NICKNAME,
                avatarUrl = null,
                avatarVersion = 0
            )
        }

        val avatarUrl = resolveAvatarUrl(row.avatarPath)
        return row.toDomain(avatarUrl)
    }

    override suspend fun updateNickname(newNickname: String): UserProfile {
        val userId = currentUserId()
        val nickname = newNickname.ifBlank { DEFAULT_NICKNAME }

        val existingRows = supabase.postgrest["app_user_profiles"]
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<UserProfileDto>()

        val existing = existingRows.firstOrNull()
        val path = existing?.avatarPath ?: avatarPath(userId)
        val version = existing?.avatarVersion ?: 0

        val upsertRow = UserProfileDto(
            userId = userId,
            nickname = nickname,
            avatarPath = path,
            avatarVersion = version
        )

        val row = supabase.postgrest["app_user_profiles"]
            .upsert(upsertRow) {
                select()
            }
            .decodeSingle<UserProfileDto>()

        val avatarUrl = resolveAvatarUrl(row.avatarPath)
        return row.toDomain(avatarUrl)
    }

    override suspend fun updateAvatar(avatarUri: Uri): UserProfile {
        val userId = currentUserId()
        val path = avatarPath(userId)

        val bytes = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(avatarUri)?.use { input ->
                input.readBytes()
            } ?: error("이미지 파일을 읽을 수 없습니다.")
        }

        logger.d("ProfileRepo", "updateAvatar: user=$userId, path=$path, bytes=${bytes.size}")

        val bucket = supabase.storage.from("avatars")

        val uploadResult = try {
            bucket.upload(
                path = path,
                data = bytes
            ) {
                upsert = true
            }
        } catch (e: Exception) {
            logger.e("ProfileRepo", "avatar upload fail: $path", e)
            throw e
        }

        logger.d("ProfileRepo", "avatar upload success: $uploadResult")

        val existingRows = supabase.postgrest["app_user_profiles"]
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<UserProfileDto>()

        val existing = existingRows.firstOrNull()
        val nickname = existing?.nickname ?: DEFAULT_NICKNAME
        val newVersion = (existing?.avatarVersion ?: 0) + 1

        val upsertRow = UserProfileDto(
            userId = userId,
            nickname = nickname,
            avatarPath = path,
            avatarVersion = newVersion,
        )

        val row = supabase.postgrest["app_user_profiles"]
            .upsert(upsertRow) {
                select()
            }
            .decodeSingle<UserProfileDto>()

        val avatarUrl = resolveAvatarUrl(row.avatarPath)
        return row.toDomain(avatarUrl)
    }
}
