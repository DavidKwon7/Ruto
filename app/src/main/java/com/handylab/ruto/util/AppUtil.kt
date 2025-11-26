package com.handylab.ruto.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Play Store에서 설치된 App 버전값을 가져옴
 */
@Composable
fun getVersionName(): String {
    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    return packageInfo.versionName ?: "1.0.0"
}