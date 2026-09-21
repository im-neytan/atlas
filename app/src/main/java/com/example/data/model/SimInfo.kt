package com.example.data.model

data class SimInfo(
    val slot: Int, // 1 ou 2
    val isActive: Boolean,
    val carrierName: String,
    val remainingSends: Int,
    val totalLimit: Int,
    val isInserted: Boolean = true,
    val isDefaultVoice: Boolean = false,
    val subscriptionId: Int = 1
) {
    val isLimitReached: Boolean
        get() = remainingSends <= 0

    val progressRatio: Float
        get() = if (totalLimit > 0) remainingSends.toFloat() / totalLimit.toFloat() else 0f
}

data class DeviceStatusReport(
    val activeSim: Int,
    val sim1Remaining: Int,
    val sim1Limit: Int,
    val sim2Remaining: Int,
    val sim2Limit: Int,
    val queueCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
