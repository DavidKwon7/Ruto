package com.handylab.ruto.ui.setting.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * 공용 아바타 이미지 컴포넌트
 *
 * @param avatarUrl 서버 signed URL 또는 로컬 Uri.toString()
 * @param cacheKey  동일 유저에 대해 항상 같은 값이면, URL이 바뀌어도 디스크 캐시 재사용 가능
 *                  ex) "avatar_${userId}_${avatarVersion}"
 */
@Composable
fun AvatarImage(
    modifier: Modifier = Modifier,
    avatarUrl: String?,
    cacheKey: String? = null,
    size: Dp = 96.dp,
) {
    val context = LocalContext.current

    val model = remember(avatarUrl, cacheKey) {
        ImageRequest.Builder(context)
            .data(avatarUrl)
            .apply {
                if (cacheKey != null && avatarUrl != null && avatarUrl.startsWith("http")) {
                    diskCacheKey(cacheKey)
                    memoryCacheKey(cacheKey)
                }
            }
            .crossfade(true)
            .build()
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl == null) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "기본 프로필",
            )
        } else {
            AsyncImage(
                model = model,
                contentDescription = "프로필 이미지",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}