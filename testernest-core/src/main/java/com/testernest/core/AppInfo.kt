package com.testernest.core

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build

data class AppInfo(
    val packageName: String,
    val appVersion: String,
    val buildNumber: String,
    val deviceModel: String,
    val osVersion: String
) {
    companion object {
        fun from(context: Context): AppInfo {
            val packageName = context.packageName
            val info: PackageInfo = context.packageManager.getPackageInfo(packageName, 0)
            val versionName = info.versionName ?: ""
            val versionCode = if (Build.VERSION.SDK_INT >= 28) {
                info.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                info.versionCode.toString()
            }
            val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}".trim()
            val osVersion = "Android ${Build.VERSION.RELEASE}"
            return AppInfo(
                packageName = packageName,
                appVersion = versionName,
                buildNumber = versionCode,
                deviceModel = deviceModel,
                osVersion = osVersion
            )
        }
    }
}
