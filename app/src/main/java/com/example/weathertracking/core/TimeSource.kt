package com.example.weathertracking.core

/** Abstraction over the wall clock so time-based rules can be unit tested. */
fun interface TimeSource {
    fun nowEpochSeconds(): Long
}
