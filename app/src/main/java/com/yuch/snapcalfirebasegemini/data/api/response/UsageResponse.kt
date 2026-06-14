package com.yuch.snapcalfirebasegemini.data.api.response

data class UsageResponse(
    val tier: String,
    val subscriptionStatus: String,
    val usage: UsageDetail,
    val byokActive: Boolean? = null
)

data class UsageDetail(
    val chat: QuotaInfo,
    val image: QuotaInfo,
    val recommendation: QuotaInfo
)

data class QuotaInfo(
    val used: Int,
    val limit: Int,
    val remaining: Int,
    val baseLimit: Int,
    val bonusQuota: Int
)
