package gg.op.gameflix.util

import org.mockito.Mockito

internal fun <T> any(type: Class<T>): T = Mockito.any(type)

internal fun <T> eq(value: T): T = Mockito.eq(value)