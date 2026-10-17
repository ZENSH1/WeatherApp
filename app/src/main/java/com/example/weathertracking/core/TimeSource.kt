package com.example.weathertracking.core

/** The clock, replaceable in tests. */
fun interface TimeSource {
    fun nowEpochSeconds(): Long
}
