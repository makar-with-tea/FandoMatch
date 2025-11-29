package ru.hse.fandomatch.ui.utils

fun Boolean?.orFalse(): Boolean = this ?: false

fun Int?.orZero(): Int = this ?: 0

fun String?.orEmpty(): String = this ?: ""

fun <T> T?.orDefault(default: T): T = this ?: default
