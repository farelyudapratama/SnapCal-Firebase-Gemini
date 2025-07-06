package com.yuch.snapcalfirebasegemini.utils

fun String.normalizeDecimal(): String {
    return this.trim().replace(" ", "").replace(",", ".")
}