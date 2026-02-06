package com.yuch.snapcalfirebasegemini.data.api.response

data class Announcement(
    val id: String,
    val title: String,
    val message: String,
    val type: String, // 'info', 'promo', 'survey', 'warning', 'update'
    val actionType: String, // 'none', 'link', 'button'
    val actionUrl: String?,
    val actionText: String?,
    val priority: Int
)
