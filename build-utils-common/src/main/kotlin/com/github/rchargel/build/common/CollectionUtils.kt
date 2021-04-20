package com.github.rchargel.build.common

fun <T : Any> Collection<T>.firstOr(defaultValue: T, predicate: (T) -> Boolean) = this.firstOrNull(predicate) ?: defaultValue
fun <T : Any> Collection<T>.firstOr(defaultValue: T): T = this.firstOr(defaultValue) { true }
fun <T : Any> Collection<T>.lastOr(defaultValue: T, predicate: (T) -> Boolean) = this.lastOrNull(predicate) ?: defaultValue
fun <T : Any> Collection<T>.lastOr(defaultValue: T): T = this.lastOr(defaultValue) { true }
