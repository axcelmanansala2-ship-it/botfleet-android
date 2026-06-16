package com.botfleet.android.data.api.models

import com.google.gson.annotations.SerializedName

data class Bot(
    val id: String,
    val name: String,
    val status: String,
    val entryFile: String,
    val uploadedAt: String,
    val pid: Int?,
    val uptime: Double?,
    val cpuPercent: Double?,
    val memMb: Double?,
    val installedPackages: List<String>,
    val autoRestart: Boolean,
    val crashCount: Int
)

data class BotStats(
    val totalBots: Int,
    val runningBots: Int,
    val stoppedBots: Int,
    val crashedBots: Int,
    val totalCrashes: Int
)

data class SystemStats(
    val cpuPercent: Double,
    val memUsedMb: Double,
    val memTotalMb: Double,
    val memPercent: Double,
    val diskUsedGb: Double,
    val diskTotalGb: Double,
    val diskPercent: Double
)

data class LogsResponse(
    val logs: List<String>
)

data class UpdateBotRequest(
    val name: String? = null,
    val autoRestart: Boolean? = null
)

data class SuccessResponse(
    val success: Boolean
)

data class InstallStatus(
    val status: String,
    val packages: List<String>? = null,
    val error: String? = null
)
